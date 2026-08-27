package com.focusforge.native;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;

/**
 * Simple launcher activity that opens accessibility settings
 * to help users enable FocusForge's accessibility service.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Open accessibility settings for the user to enable FocusForge
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        
        // Close this activity
        finish();
    }
}
