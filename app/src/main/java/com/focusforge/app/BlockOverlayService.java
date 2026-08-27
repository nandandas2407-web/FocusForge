package com.focusforge.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlockOverlayService extends Service {
    private static final String TAG = "FocusForge";
    private static final String CHANNEL_ID = "focusforge_overlay";
    private WindowManager windowManager;
    private View overlayView;
    private static BlockOverlayService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    public static void dismissCurrent() {
        if (instance != null) {
            instance.dismissOverlay();
        }
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
            Log.w(TAG, "No overlay permission");
            stopSelf();
            return START_NOT_STICKY;
        }

        dismissOverlay();
        showOverlay(reason);

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
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setBackgroundColor(0xF007070C);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(32), dp(32), dp(32), dp(32));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(24), dp(32), dp(24), dp(32));
        card.setBackgroundResource(R.drawable.card_bg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.gravity = Gravity.CENTER;
        card.setLayoutParams(cardParams);

        TextView blocked = new TextView(this);
        blocked.setText("BLOCKED");
        blocked.setTextColor(0xFFF44336);
        blocked.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        blocked.setTypeface(null, Typeface.BOLD);
        blocked.setGravity(Gravity.CENTER);
        card.addView(blocked);

        TextView msg = new TextView(this);
        msg.setText(reason);
        msg.setTextColor(0xFFCCCCCC);
        msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, dp(16), 0, 0);
        card.addView(msg);

        Button dismissBtn = new Button(this);
        dismissBtn.setText("DISMISS");
        dismissBtn.setTextColor(0xFF07070C);
        dismissBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        dismissBtn.setTypeface(null, Typeface.BOLD);
        dismissBtn.setBackgroundResource(R.drawable.button_gold_bg);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            dp(200),
            dp(48)
        );
        btnParams.gravity = Gravity.CENTER;
        btnParams.topMargin = dp(24);
        dismissBtn.setLayoutParams(btnParams);
        dismissBtn.setOnClickListener(v -> {
            dismissOverlay();
            stopSelf();
        });
        card.addView(dismissBtn);

        outer.addView(card);
        return outer;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
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
            channel.setDescription("Block notifications");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        instance = null;
        dismissOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
