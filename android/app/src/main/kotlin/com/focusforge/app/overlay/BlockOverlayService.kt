// ============================================================
// FILE: android/.../overlay/BlockOverlayService.kt
// PURPOSE: Shows a full-screen overlay when an app is blocked,
//          explaining why and providing optional unlock flow.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-06
// ============================================================
package com.focusforge.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.focusforge.app.MainActivity

class BlockOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "focusforge_overlay"
        private const val NOTIFICATION_ID = 1001
        private const val OVERLAY_DISMISS_DELAY_MS = 3000L
        private const val UNLOCK_GRACE_PERIOD_MS = 300_000L // 5 minutes

        private var overlayView: View? = null
        private var windowManager: WindowManager? = null
        private var dismissTimer: CountDownTimer? = null
        private var unlockTimer: CountDownTimer? = null

        /** Packages temporarily unlocked via the "Unlock for 5 min" button. */
        private val temporarilyUnlocked = mutableSetOf<String>()

        fun isTemporarilyUnlocked(packageName: String): Boolean =
            temporarilyUnlocked.contains(packageName)

        fun show(
            context: Context,
            packageName: String,
            reason: String,
            isStrictMode: Boolean
        ) {
            val intent = Intent(context, BlockOverlayService::class.java).apply {
                putExtra("package_name", packageName)
                putExtra("reason", reason)
                putExtra("is_strict_mode", isStrictMode)
                putExtra("overlay_type", "blocking")
            }
            context.startForegroundService(intent)
        }

        fun showTransient(
            context: Context,
            packageName: String,
            subScreen: String,
            reason: String
        ) {
            val intent = Intent(context, BlockOverlayService::class.java).apply {
                putExtra("package_name", packageName)
                putExtra("sub_screen", subScreen)
                putExtra("reason", reason)
                putExtra("is_strict_mode", false)
                putExtra("overlay_type", "transient")
            }
            context.startForegroundService(intent)
        }

        fun removeOverlayStatic() {
            dismissTimer?.cancel()
            overlayView?.let { view ->
                try {
                    windowManager?.removeView(view)
                } catch (_: Exception) {}
            }
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("package_name") ?: ""
        val reason = intent?.getStringExtra("reason") ?: "This app is blocked"
        val isStrictMode = intent?.getBooleanExtra("is_strict_mode", false) ?: false
        val overlayType = intent?.getStringExtra("overlay_type") ?: "blocking"
        val subScreen = intent?.getStringExtra("sub_screen")

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        showOverlay(packageName, reason, isStrictMode, overlayType, subScreen)

        return START_NOT_STICKY
    }

    private fun showOverlay(
        packageName: String,
        reason: String,
        isStrictMode: Boolean,
        overlayType: String,
        subScreen: String?
    ) {
        removeOverlay()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layout = createOverlayLayout(packageName, reason, isStrictMode, overlayType, subScreen)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        overlayView = layout
        windowManager?.addView(layout, params)

        if (overlayType == "transient") {
            dismissTimer = object : CountDownTimer(OVERLAY_DISMISS_DELAY_MS, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    removeOverlay()
                    stopSelf()
                }
            }.start()
        }
    }

    private fun createOverlayLayout(
        packageName: String,
        reason: String,
        isStrictMode: Boolean,
        overlayType: String,
        subScreen: String?
    ): View {
        val density = resources.displayMetrics.density

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                (32 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt()
            )
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#CC0B0D12"))
                cornerRadius = 0f
            }
        }

        // Glass card container
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                (32 * density).toInt(),
                (40 * density).toInt(),
                (32 * density).toInt(),
                (40 * density).toInt()
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#22FFFFFF"))
                cornerRadius = 28 * density
                setStroke(
                    (1 * density).toInt(),
                    Color.parseColor("#40FFFFFF")
                )
            }
            elevation = 8 * density
        }

        // Title
        val title = TextView(this).apply {
            text = if (overlayType == "transient") {
                "Blocked: $subScreen"
            } else {
                "Focus Active"
            }
            setTextColor(Color.parseColor("#F5F6FA"))
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (16 * density).toInt())
        }

        // Reason
        val reasonText = TextView(this).apply {
            text = reason
            setTextColor(Color.parseColor("#A7ACC0"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (24 * density).toInt())
        }

        // App name
        val appText = TextView(this).apply {
            text = "App: $packageName"
            setTextColor(Color.parseColor("#7C5CFF"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (32 * density).toInt())
        }

        container.addView(card)
        card.addView(title)
        card.addView(reasonText)
        card.addView(appText)

        if (!isStrictMode && overlayType != "transient") {
            // Button row: Unlock + Remove Overlay side by side
            val buttonRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            // Unlock for 5 min button
            val unlockButton = Button(this).apply {
                text = "Unlock for 5 min"
                setTextColor(Color.WHITE)
                textSize = 13f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#7C5CFF"))
                    cornerRadius = 16 * density
                }
                setPadding(
                    (20 * density).toInt(),
                    (12 * density).toInt(),
                    (20 * density).toInt(),
                    (12 * density).toInt()
                )
                setOnClickListener {
                    // Temporarily unlock this package for 5 minutes
                    temporarilyUnlocked.add(packageName)
                    unlockTimer?.cancel()
                    unlockTimer = object : CountDownTimer(UNLOCK_GRACE_PERIOD_MS, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            temporarilyUnlocked.remove(packageName)
                        }
                    }.start()
                    removeOverlay()
                    stopSelf()
                }
            }

            // Remove overlay button (only dismisses overlay, blocking stays)
            val removeButton = Button(this).apply {
                text = "Dismiss"
                setTextColor(Color.parseColor("#A7ACC0"))
                textSize = 13f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#22FFFFFF"))
                    cornerRadius = 16 * density
                    setStroke((1 * density).toInt(), Color.parseColor("#40FFFFFF"))
                }
                setPadding(
                    (20 * density).toInt(),
                    (12 * density).toInt(),
                    (20 * density).toInt(),
                    (12 * density).toInt()
                )
                setOnClickListener {
                    // Just remove the overlay — blocking stays active
                    removeOverlay()
                    stopSelf()
                }
            }

            buttonRow.addView(unlockButton)
            val spacer = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (12 * density).toInt(),
                    0
                )
            }
            buttonRow.addView(spacer)
            buttonRow.addView(removeButton)
            card.addView(buttonRow)
        }

        if (isStrictMode) {
            val strictNotice = TextView(this).apply {
                text = "Strict Mode is active — session must complete"
                setTextColor(Color.parseColor("#FF5C7C"))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, (8 * density).toInt(), 0, 0)
            }
            card.addView(strictNotice)
        }

        return container
    }

    private fun removeOverlay() {
        dismissTimer?.cancel()
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        overlayView = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Block Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when an app is blocked"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusForge")
            .setContentText("Focus session active")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
