// ============================================================
// FILE: app/src/main/java/com/example/service/BlockOverlayService.kt
// PURPOSE: Foreground Service displaying minimal green overlay when content is blocked.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Brutal minimalism overhaul.
// ============================================================

package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.db.FocusDatabase
import com.example.data.entity.BlockedAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME") ?: "App"
        val appName = intent?.getStringExtra("APP_NAME") ?: packageName.substringAfterLast('.')
        val reason = intent?.getStringExtra("REASON") ?: "Focus Mode is Active"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusForge")
            .setContentText("Blocked $appName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            showOverlay(packageName, appName, reason)
        }

        return START_NOT_STICKY
    }

    private fun showOverlay(pkgName: String, appName: String, reason: String) {
        removeOverlay()

        val ctx = this
        val px = { dp: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
            ).toInt()
        }

        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(20), px(20), px(20), px(20))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#111916"))
                cornerRadius = px(12).toFloat()
                setStroke(px(1), Color.parseColor("#14FFFFFF"))
            }
        }

        // Green dot indicator
        val dot = View(ctx).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#22C55E"))
            }
            layoutParams = LinearLayout.LayoutParams(px(8), px(8))
        }
        card.addView(dot)

        // Title
        val title = TextView(ctx).apply {
            text = appName
            setTextColor(Color.parseColor("#E8F0EA"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, px(8), 0, px(4))
        }
        card.addView(title)

        // Reason
        val body = TextView(ctx).apply {
            text = reason
            setTextColor(Color.parseColor("#8A9B8E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, 0, 0, px(16))
        }
        card.addView(body)

        // Buttons
        val btnRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        val dismissBtn = Button(ctx).apply {
            text = "Go Home"
            setTextColor(Color.parseColor("#8A9B8E"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#182019"))
                cornerRadius = px(8).toFloat()
            }
            setOnClickListener {
                try {
                    ctx.startActivity(Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {}
                removeOverlay()
                stopSelf()
            }
        }

        val unblockBtn = Button(ctx).apply {
            text = "Allow"
            setTextColor(Color.parseColor("#0A0F0D"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#22C55E"))
                cornerRadius = px(8).toFloat()
            }
            setOnClickListener {
                serviceScope.launch {
                    try {
                        val dao = FocusDatabase.getDatabase(ctx).focusDao()
                        dao.insertBlockedApp(
                            BlockedAppEntity(
                                packageName = pkgName,
                                appName = appName,
                                isFullyBlocked = false,
                                isReelsBlocked = false,
                                isShortsBlocked = false
                            )
                        )
                    } catch (_: Exception) {}
                }
                removeOverlay()
                stopSelf()
            }
        }

        btnRow.addView(dismissBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        btnRow.addView(unblockBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(px(8), 0, 0, 0) })
        card.addView(btnRow)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            px(300), WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        try {
            windowManager?.addView(card, params)
            overlayView = card
        } catch (_: Exception) {}
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "FocusForge",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "focusforge_overlay"
        const val NOTIFICATION_ID = 1001
    }
}
