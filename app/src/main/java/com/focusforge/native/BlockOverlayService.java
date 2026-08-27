package com.focusforge.native;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * Foreground service that shows a block overlay notification
 * when a blocked app/content is detected.
 */
public class BlockOverlayService extends Service {

    private static final String CHANNEL_ID = "focusforge_block_overlay";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String packageName = intent != null ? intent.getStringExtra("PACKAGE_NAME") : "unknown";
        String reason = intent != null ? intent.getStringExtra("REASON") : "Content blocked";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("FocusForge")
            .setContentText(reason)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build();

        startForeground(NOTIFICATION_ID, notification);

        // Auto-dismiss after 3 seconds
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            stopForeground(true);
            stopSelf();
        }, 3000);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Block Overlay",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows when content is blocked");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
