// ============================================================
// FILE: app/src/main/java/com/example/service/FocusDeviceAdminReceiver.kt
// PURPOSE: Device Admin Receiver for optional Strict Mode uninstall protection.
// CREATED: 2026-08-09
// ============================================================

package com.example.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class FocusDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "FocusForge Uninstall Guard Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "FocusForge Uninstall Guard Disabled", Toast.LENGTH_SHORT).show()
    }
}
