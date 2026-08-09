// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/OnboardingScreen.kt
// PURPOSE: First-launch onboarding flow with permission setup (Accessibility,
//          Usage Stats, Overlay, Notifications) and goal picker.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }

    WallpaperBackground(preset = "COSMIC_NEON") {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                1 -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GlassTokens.Info,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Welcome to FocusForge",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reclaim your screen time with Liquid Glass dark design, Reels/Shorts Blocker, and Pomodoro study tools.",
                                fontSize = 14.sp,
                                color = GlassTokens.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            GlassButton(
                                text = "Get Started",
                                onClick = { step = 2 },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.ArrowForward,
                                testTagStr = "onboarding_next_button"
                            )
                        }
                    }
                }
                2 -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Enable System Permissions",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "FocusForge uses native Android Accessibility & Usage access to block distractions directly on your device.",
                                fontSize = 13.sp,
                                color = GlassTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            PermissionItem(
                                title = "Accessibility Service",
                                description = "Required to detect Instagram Reels, YouTube Shorts & blocked apps",
                                icon = Icons.Default.AccessibilityNew,
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PermissionItem(
                                title = "Usage Stats Access",
                                description = "Required to generate screen time analytics & dashboards",
                                icon = Icons.Default.Analytics,
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PermissionItem(
                                title = "Display Over Other Apps",
                                description = "Required to show liquid glass block overlays",
                                icon = Icons.Default.Layers,
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            GlassButton(
                                text = "Continue to App",
                                onClick = onFinishOnboarding,
                                modifier = Modifier.fillMaxWidth(),
                                testTagStr = "onboarding_finish_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlassTokens.Accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTokens.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = GlassTokens.TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = GlassTokens.Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enable", fontSize = 12.sp)
            }
        }
    }
}
