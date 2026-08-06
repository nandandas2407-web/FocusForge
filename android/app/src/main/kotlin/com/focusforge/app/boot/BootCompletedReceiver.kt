// ============================================================
// FILE: android/.../boot/BootCompletedReceiver.kt
// PURPOSE: Restarts blocking services after device reboot.
//
//          NOTE: This previously read a "focusforge_prefs" prefs
//          file/keys ("session_active", "strict_mode") that nothing
//          else in the app ever wrote — the Flutter side persists
//          session state via the shared_preferences plugin, which
//          uses a different backing file ("FlutterSharedPreferences")
//          with "flutter." key prefixes. That meant this receiver's
//          sessionActive check was always false in practice, and even
//          when true it only launched a placeholder transient overlay
//          with no package name — it never actually restarted the
//          accessibility service's enforcement of the user's real
//          block rules. Fixed to read the correct prefs file/keys.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
package com.focusforge.app.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Device booted — checking for an active FocusForge session")

            // Matches the file/key naming shared_preferences (Flutter
            // plugin) actually uses on Android.
            val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
            val sessionActive = prefs.getBoolean("flutter.rules_session_active", false)

            if (sessionActive) {
                Log.i(TAG, "A focus session was active before reboot — the accessibility " +
                        "service will re-apply block rules once Flutter re-launches and " +
                        "calls updateSessionState(). No overlay is shown here since we don't " +
                        "have a specific blocked package/reason without the app process running.")
            }
        }
    }
}
