// ============================================================
// FILE: android/.../boot/BootCompletedReceiver.kt
// PURPOSE: Restarts blocking services after device reboot by
//          reading persisted session state from FlutterSharedPreferences
//          and pushing it to the accessibility service.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-06
// ============================================================
package com.focusforge.app.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.focusforge.app.accessibility.FocusAccessibilityService

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
        private const val PREFS_NAME = "FlutterSharedPreferences"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Device booted — checking for an active FocusForge session")

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val sessionActive = prefs.getBoolean("flutter.rules_session_active", false)

            if (sessionActive) {
                Log.i(TAG, "A focus session was active before reboot — " +
                        "the accessibility service will restore state when it connects.")

                // The accessibility service reads from the same prefs file
                // in restoreSessionState() when onServiceConnected() fires.
                // No need to start it manually — Android will reconnect it
                // if it was enabled before reboot. We just log for awareness.
            } else {
                Log.i(TAG, "No active session — nothing to restore")
            }
        }
    }
}
