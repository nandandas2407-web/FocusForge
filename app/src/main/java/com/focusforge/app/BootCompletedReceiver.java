package com.focusforge.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "FocusForge";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, checking accessibility service");
            FocusForgeConfig.init(context);

            String serviceName = context.getPackageName() + "/com.focusforge.app.FocusForgeAccessibilityService";
            String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );

            if (enabledServices != null && enabledServices.contains(serviceName)) {
                Log.d(TAG, "Accessibility service was enabled, user needs to re-enable after boot");
            } else {
                Log.d(TAG, "Accessibility service not enabled, user will need to enable manually");
            }
        }
    }
}
