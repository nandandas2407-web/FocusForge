// ============================================================
// FILE: app/src/main/java/com/example/service/BlockOverlayService.kt
// PURPOSE: Foreground Service displaying system overlay window and alert notification
//          when apps or Reels/Shorts are blocked in Focus Mode.
// CREATED: 2026-08-09
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
            .setContentTitle("FocusForge Protection")
            .setContentText("Blocked access to $appName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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

        // Show Window Overlay if overlay permission is granted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            showSystemOverlay(packageName, appName, reason)
        }

        return START_NOT_STICKY
    }

    private fun showSystemOverlay(pkgName: String, appName: String, reason: String) {
        removeOverlayView()

        val context = this
        val dpToPx = { dp: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                resources.displayMetrics
            ).toInt()
        }

        // Parent container card
        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B")) // Slate 800
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#33FFFFFF"))
            }
        }

        // Header
        val headerTv = TextView(context).apply {
            text = "🛡️ FocusForge Protection"
            setTextColor(Color.parseColor("#F8FAFC"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        cardLayout.addView(headerTv)

        // Body message
        val bodyTv = TextView(context).apply {
            text = "You have blocked $appName.\n$reason"
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, dpToPx(8), 0, dpToPx(16))
        }
        cardLayout.addView(bodyTv)

        // Button row
        val btnRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        // Dismiss Button
        val dismissBtn = Button(context).apply {
            text = "Dismiss"
            setTextColor(Color.parseColor("#94A3B8"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#334155"))
                cornerRadius = dpToPx(8).toFloat()
            }
            setOnClickListener {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(homeIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                removeOverlayView()
                stopSelf()
            }
        }

        // Turn Off Block Button
        val unblockBtn = Button(context).apply {
            text = "Turn Off Block"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#6366F1")) // Indigo
                cornerRadius = dpToPx(8).toFloat()
            }
            setOnClickListener {
                // Update Room Database to unblock app
                serviceScope.launch {
                    try {
                        val dao = FocusDatabase.getDatabase(context).focusDao()
                        dao.insertBlockedApp(
                            BlockedAppEntity(
                                packageName = pkgName,
                                appName = appName,
                                isFullyBlocked = false,
                                isReelsBlocked = false,
                                isShortsBlocked = false
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                removeOverlayView()
                stopSelf()
            }
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dpToPx(8), 0, 0, 0)
        }

        btnRow.addView(dismissBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        btnRow.addView(unblockBtn, btnParams)
        cardLayout.addView(btnRow)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            dpToPx(340),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager?.addView(cardLayout, params)
            overlayView = cardLayout
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusForge Protection Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "focus_forge_overlay_channel"
        const val NOTIFICATION_ID = 1001
    }
}
