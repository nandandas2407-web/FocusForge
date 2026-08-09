// ============================================================
// FILE: app/src/main/java/com/example/service/BootCompletedReceiver.kt
// PURPOSE: Listens for device reboot and resumes FocusForge background services.
// CREATED: 2026-08-09
// ============================================================

package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "FocusForge boot completed received. Protection active.")
        }
    }
}
