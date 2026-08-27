package com.focusforge.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlockOverlayService extends Service {
    private static final String TAG = "FocusForge";
    private static final String CHANNEL_ID = "focusforge_overlay";
    private WindowManager windowManager;
    private View overlayView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String reason = intent.getStringExtra("REASON");
        if (reason == null) reason = "Content blocked";

        try {
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("FocusForge")
                .setContentText(reason)
                .setSmallIcon(R.drawable.ic_dashboard)
                .build();
            startForeground(1, notification);
        } catch (Exception e) {
            Log.e(TAG, "startForeground failed", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            Log.w(TAG, "No overlay permission, skipping overlay");
            stopSelf();
            return START_NOT_STICKY;
        }

        showOverlay(reason);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismissOverlay();
            stopSelf();
        }, 2500);

        return START_NOT_STICKY;
    }

    private void showOverlay(String reason) {
        try {
            overlayView = createOverlayView(reason);
            int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.CENTER;

            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            Log.e(TAG, "showOverlay failed", e);
        }
    }

    private View createOverlayView(String reason) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(0xCC07070C);
        container.setGravity(Gravity.CENTER);
        container.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText("BLOCKED");
        title.setTextColor(0xFFC9A84C);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        container.addView(title);

        TextView msg = new TextView(this);
        msg.setText(reason);
        msg.setTextColor(0xFF999999);
        msg.setTextSize(16);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, 24, 0, 0);
        container.addView(msg);

        TextView hint = new TextView(this);
        hint.setText("Returning to home...");
        hint.setTextColor(0xFF666666);
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 32, 0, 0);
        container.addView(hint);

        return container;
    }

    private void dismissOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.e(TAG, "dismissOverlay failed", e);
            }
            overlayView = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "FocusForge Overlays",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows block notifications");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        dismissOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
