package com.focusforge.native;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Device Admin receiver for uninstall protection.
 * Prevents casual uninstallation during focus sessions.
 */
public class FocusDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "FocusForge: Uninstall protection enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "FocusForge: Uninstall protection disabled", Toast.LENGTH_SHORT).show();
    }
}
