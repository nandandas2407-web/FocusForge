// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/StatsDashboardScreen.kt
// PURPOSE: Usage stats dashboard with screen time charts & real app usage breakdown.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.data.repository.AppUsageInfo
import com.example.ui.theme.*

@Composable
fun StatsDashboardScreen(
    usageStats: List<AppUsageInfo>,
    focusSessions: List<FocusSessionEntity> = emptyList()
) {
    val totalFocusMs = focusSessions.filter { it.isCompleted }.sumOf { it.durationMinutes * 60000L }
    val focusHours = (totalFocusMs / 3600000).toInt()
    val focusMins = ((totalFocusMs % 3600000) / 60000).toInt()

    val totalUsageMs = usageStats.sumOf { it.totalTimeInForegroundMs }
    val usageHours = (totalUsageMs / 3600000).toInt()
    val usageMins = ((totalUsageMs % 3600000) / 60000).toInt()

    WallpaperBackground(preset = "COSMIC_NEON") {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth >= 600.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = if (isTablet) 32.dp else 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = GlassTokens.NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Screen Time Analytics",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Track app usage metrics and time reclaimed with FocusForge",
                                fontSize = 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                // Focus Time Reclaimed Hero Card
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = GlassTokens.VibrantGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (totalFocusMs > 0) "${focusHours}h ${focusMins}m Focus Time Completed" else "Start a session to track focus time",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Total Tracked Foreground Screen Time Today: ${usageHours}h ${usageMins}m",
                                        fontSize = 12.sp,
                                        color = GlassTokens.VibrantGreen
                                    )
                                }
                            }

                            if (totalUsageMs == 0L) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "To track live foreground app usage time on your device, enable Usage Access in Android Settings.",
                                    fontSize = 12.sp,
                                    color = GlassTokens.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                GlassButton(
                                    text = "Enable Usage Access",
                                    onClick = {
                                        try {
                                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Today's App Breakdown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                }

                if (usageStats.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No app usage detected today or Usage Access permission needs to be enabled in System Settings.",
                                fontSize = 13.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                } else {
                    items(usageStats.size) { idx ->
                        val app = usageStats[idx]
                        val minutes = app.totalTimeInForegroundMs / 60000
                        val maxMinutes = (totalUsageMs / 60000).coerceAtLeast(1)
                        val progress = (minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.05f, 1f)

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = app.appName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "${minutes}m",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.NeonCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GlassTokens.DarkBase)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GlassTokens.ElectricViolet)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
