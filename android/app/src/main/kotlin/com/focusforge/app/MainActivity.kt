// ============================================================
// FILE: android/.../MainActivity.kt
// PURPOSE: Main Flutter activity — initializes native bridges
//          and handles MethodChannel calls from Flutter.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
package com.focusforge.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.focusforge.app.accessibility.FocusAccessibilityService
import com.focusforge.app.usage.UsageStatsBridge
import com.focusforge.app.vpn.FocusVpnService

class MainActivity : FlutterActivity() {

    private val METHOD_CHANNEL = "com.focusforge.app/native"
    private lateinit var usageStatsBridge: UsageStatsBridge

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        usageStatsBridge = UsageStatsBridge(this)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "isAccessibilityEnabled" -> result.success(isAccessibilityEnabled())
                "openAccessibilitySettings" -> {
                    openAccessibilitySettings()
                    result.success(true)
                }
                "isUsageStatsEnabled" -> result.success(usageStatsBridge.isUsageStatsAvailable())
                "openUsageStatsSettings" -> {
                    openUsageStatsSettings()
                    result.success(true)
                }
                "isOverlayEnabled" -> result.success(Settings.canDrawOverlays(this))
                "openOverlaySettings" -> {
                    openOverlaySettings()
                    result.success(true)
                }
                "getDailyScreenTime" -> result.success(usageStatsBridge.getTotalScreenTimeToday())
                "getWeeklyScreenTime" -> result.success(usageStatsBridge.getWeeklyScreenTime())
                "getMonthlyScreenTime" -> result.success(usageStatsBridge.getMonthlyScreenTime())
                "getDailyBreakdown" -> {
                    val days = call.argument<Int>("days") ?: 7
                    val buckets = usageStatsBridge.getDailyBreakdown(days)
                    result.success(buckets.map { mapOf(
                        "dateKey" to it.dateKey,
                        "totalTimeMs" to it.totalTimeMs
                    )})
                }
                "getTopApps" -> {
                    val limit = call.argument<Int>("limit") ?: 10
                    val stats = usageStatsBridge.getTopApps(limit)
                    result.success(stats.map { mapOf(
                        "packageName" to it.packageName,
                        "totalTimeMs" to it.totalTimeMs,
                        "lastTimeUsed" to it.lastTimeUsed,
                        "appLabel" to it.appLabel
                    )})
                }
                "getInstalledApps" -> {
                    val apps = usageStatsBridge.getInstalledApps()
                    result.success(apps.map { mapOf(
                        "packageName" to it.packageName,
                        "totalTimeMs" to it.totalTimeMs,
                        "lastTimeUsed" to it.lastTimeUsed,
                        "appLabel" to it.appLabel
                    )})
                }
                "updateSessionState" -> {
                    val active = call.argument<Boolean>("active") ?: false
                    val blockedPackages = (call.argument<List<String>>("blockedPackages") ?: emptyList()).toSet()
                    val shortsBlockedPackages = (call.argument<List<String>>("shortsBlockedPackages") ?: emptyList()).toSet()
                    val strictMode = call.argument<Boolean>("strictMode") ?: false
                    val ytStudyMode = call.argument<Boolean>("youtubeStudyMode") ?: false
                    val ytWhitelist = call.argument<List<String>>("youtubeWhitelist") ?: emptyList()
                    FocusAccessibilityService.instance?.updateSessionState(
                        active = active,
                        blockedPkgs = blockedPackages,
                        shortsBlockedPkgs = shortsBlockedPackages,
                        strictMode = strictMode,
                        ytStudyMode = ytStudyMode,
                        ytWhitelist = ytWhitelist
                    )
                    result.success(true)
                }
                "getBlockEventCount" -> {
                    result.success(FocusAccessibilityService.blockEventCount)
                }
                "startVpnService" -> {
                    val intent = Intent(this, FocusVpnService::class.java)
                    startService(intent)
                    result.success(true)
                }
                "stopVpnService" -> {
                    val intent = Intent(this, FocusVpnService::class.java).apply {
                        putExtra("stop", true)
                    }
                    startService(intent)
                    result.success(true)
                }
                "setBlockedDomains" -> {
                    val domains = call.argument<List<String>>("domains") ?: emptyList()
                    FocusVpnService.setBlockedDomains(domains.toSet())
                    result.success(true)
                }
                "isVpnRunning" -> result.success(FocusVpnService.isRunning)
                "requestIgnoreBatteryOptimization" -> {
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "${packageName}/com.focusforge.app.accessibility.FocusAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains(service)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun openUsageStatsSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

}
