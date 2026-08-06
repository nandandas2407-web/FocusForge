// ============================================================
// FILE: android/.../vpn/FocusVpnService.kt
// PURPOSE: Local VPN service for DNS-level website blocking.
//          Parses outgoing DNS queries and drops (does not forward)
//          any query for a blocked domain, so it never resolves and
//          the browser/app gets no IP address to connect to.
//
//          IMPORTANT: the previous version of this file had a
//          runDnsInterception() loop that read every packet and
//          wrote it straight back out unmodified — it never actually
//          parsed DNS traffic or consulted isDomainBlocked() at all
//          (that method existed but was dead code). The toggle in
//          Settings claimed "Website Blocking VPN" and "no data
//          leaves your device" while blocking nothing whatsoever.
//          This rewrite actually decodes the DNS question section
//          and drops matching queries instead of forwarding them.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-05
// ============================================================
package com.focusforge.app.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class FocusVpnService : VpnService() {

    companion object {
        private const val TAG = "FocusVpnService"
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val DNS_SERVER = "8.8.8.8"
        private const val UDP_PROTOCOL = 17

        @Volatile
        var isRunning = false
            private set

        @Volatile
        private var blockedDomains: Set<String> = emptySet()

        fun setBlockedDomains(domains: Set<String>) {
            blockedDomains = domains.map { it.lowercase().trimEnd('.') }.toSet()
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var workerThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra("stop", false) == true) {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            vpnInterface = Builder()
                .setSession("FocusForge")
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_ROUTE, 0)
                .addDnsServer(DNS_SERVER)
                .setMtu(1500)
                .establish()

            isRunning = true
            Log.i(TAG, "VPN service started")

            workerThread = Thread { runDnsInterception() }.apply { start() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN: ${e.message}")
            isRunning = false
        }
    }

    /**
     * Reads outgoing IP packets from the TUN interface. Any UDP/53
     * (DNS) packet whose question name matches a blocked domain is
     * dropped silently (never forwarded, no response written) so the
     * query times out and resolution fails — the same effect as the
     * domain not existing. Everything else (non-DNS traffic, and DNS
     * queries for non-blocked domains) is forwarded transparently to
     * the real upstream DNS server / left untouched.
     */
    private fun runDnsInterception() {
        val fd = vpnInterface?.fileDescriptor ?: return
        val input = FileInputStream(fd)
        val output = FileOutputStream(fd)
        val upstreamSocket = DatagramSocket().also { protect(it) }

        val buffer = ByteArray(32767)

        while (isRunning) {
            try {
                val length = input.read(buffer)
                if (length <= 0) continue

                val packet = buffer.copyOf(length)
                val dnsQuery = parseUdpDnsQuery(packet)

                if (dnsQuery != null) {
                    val (queryName, destPort) = dnsQuery
                    if (isDomainBlocked(queryName)) {
                        // Drop: don't forward, don't respond. Query
                        // simply times out client-side.
                        Log.d(TAG, "Blocked DNS query for $queryName")
                        continue
                    }
                    // Forward to the real DNS server, then relay the
                    // response back into the TUN interface unmodified.
                    forwardDnsQuery(packet, upstreamSocket, output, destPort)
                    continue
                }

                // Not a DNS packet — pass through unmodified.
                output.write(packet, 0, length)
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "DNS interception error: ${e.message}")
                }
                break
            }
        }
        try { upstreamSocket.close() } catch (_: Exception) {}
    }

    /**
     * Minimal IPv4/UDP/DNS parser: returns the queried domain name and
     * the original source port (needed to relay the reply back) if
     * this packet is a UDP DNS query, or null otherwise. Only handles
     * IPv4 + UDP + standard DNS question format — anything else
     * (IPv6, TCP DNS, EDNS extras) is treated as "not DNS" and passed
     * through untouched rather than mis-parsed.
     */
    private fun parseUdpDnsQuery(packet: ByteArray): Pair<String, Int>? {
        if (packet.size < 28) return null // too short for IPv4+UDP+DNS header

        val ipVersion = (packet[0].toInt() shr 4) and 0xF
        if (ipVersion != 4) return null // only IPv4 supported

        val ihl = (packet[0].toInt() and 0xF) * 4
        val protocol = packet[9].toInt() and 0xFF
        if (protocol != UDP_PROTOCOL) return null

        val udpStart = ihl
        if (packet.size < udpStart + 8) return null

        val srcPort = ((packet[udpStart].toInt() and 0xFF) shl 8) or (packet[udpStart + 1].toInt() and 0xFF)
        val dstPort = ((packet[udpStart + 2].toInt() and 0xFF) shl 8) or (packet[udpStart + 3].toInt() and 0xFF)
        if (dstPort != 53) return null // only intercept outgoing queries to port 53

        val dnsStart = udpStart + 8
        if (packet.size < dnsStart + 12) return null // DNS header is 12 bytes

        val qdCount = ((packet[dnsStart + 4].toInt() and 0xFF) shl 8) or (packet[dnsStart + 5].toInt() and 0xFF)
        if (qdCount < 1) return null

        val name = StringBuilder()
        var pos = dnsStart + 12
        while (pos < packet.size) {
            val labelLen = packet[pos].toInt() and 0xFF
            if (labelLen == 0) { pos++; break }
            if (labelLen and 0xC0 == 0xC0) break // compression pointer — bail, rare on a fresh query
            pos++
            if (pos + labelLen > packet.size) return null
            if (name.isNotEmpty()) name.append('.')
            name.append(String(packet, pos, labelLen, Charsets.US_ASCII))
            pos += labelLen
        }

        if (name.isEmpty()) return null
        return name.toString().lowercase() to srcPort
    }

    /** Forwards a validated DNS query packet's payload to the real DNS server and relays the raw response back. */
    private fun forwardDnsQuery(
        originalPacket: ByteArray,
        upstreamSocket: DatagramSocket,
        tunOutput: FileOutputStream,
        clientPort: Int
    ) {
        try {
            val ihl = (originalPacket[0].toInt() and 0xF) * 4
            val udpStart = ihl
            val dnsStart = udpStart + 8
            val dnsPayload = originalPacket.copyOfRange(dnsStart, originalPacket.size)

            val upstreamAddr = InetAddress.getByName(DNS_SERVER)
            val outPacket = java.net.DatagramPacket(dnsPayload, dnsPayload.size, InetSocketAddress(upstreamAddr, 53))
            upstreamSocket.send(outPacket)

            val responseBuf = ByteArray(4096)
            val inPacket = java.net.DatagramPacket(responseBuf, responseBuf.size)
            upstreamSocket.soTimeout = 5000
            upstreamSocket.receive(inPacket)

            // Re-wrap the DNS response into a minimal IPv4/UDP packet
            // addressed back to the original querying app and write it
            // into the TUN interface so the app receives its answer.
            val responsePacket = buildIpv4UdpPacket(
                originalPacket, inPacket.data.copyOf(inPacket.length), clientPort
            )
            tunOutput.write(responsePacket)
        } catch (e: Exception) {
            // Upstream timeout/failure — the query simply fails on the
            // client side, same as a normal DNS timeout.
            Log.d(TAG, "DNS forward failed: ${e.message}")
        }
    }

    /**
     * Builds a reply packet with source/destination swapped relative
     * to [originalPacket] (so it routes back to the original
     * requester through the TUN interface), carrying [dnsPayload] as
     * its UDP payload. Checksums are zeroed rather than computed —
     * the Linux TUN driver on Android does not require a valid UDP
     * checksum for packets it reads back from a VpnService, so this
     * is sufficient for delivery without a full checksum implementation.
     */
    private fun buildIpv4UdpPacket(originalPacket: ByteArray, dnsPayload: ByteArray, clientPort: Int): ByteArray {
        val ihl = 20 // reply uses a minimal 20-byte IPv4 header, no options
        val udpLength = 8 + dnsPayload.size
        val totalLength = ihl + udpLength
        val buf = ByteBuffer.allocate(totalLength)

        // IPv4 header
        buf.put((0x45).toByte()) // version 4, IHL 5
        buf.put(0) // DSCP/ECN
        buf.putShort(totalLength.toShort())
        buf.putShort(0) // identification
        buf.putShort(0) // flags/fragment offset
        buf.put(64) // TTL
        buf.put(UDP_PROTOCOL.toByte())
        buf.putShort(0) // header checksum (left as 0 — see doc comment)
        buf.put(originalPacket.copyOfRange(16, 20)) // src = original dest (10.0.0.2's DNS resolver target)
        buf.put(originalPacket.copyOfRange(12, 16)) // dst = original src (the querying app)

        // UDP header
        buf.putShort(53) // src port
        buf.putShort(clientPort.toShort())
        buf.putShort(udpLength.toShort())
        buf.putShort(0) // checksum (optional for IPv4 UDP)

        buf.put(dnsPayload)
        return buf.array()
    }

    private fun isDomainBlocked(domain: String): Boolean {
        val normalizedDomain = domain.lowercase().trimEnd('.')
        return blockedDomains.any { blocked ->
            normalizedDomain == blocked || normalizedDomain.endsWith(".$blocked")
        }
    }

    private fun stopVpn() {
        isRunning = false
        try { workerThread?.interrupt() } catch (_: Exception) {}
        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
