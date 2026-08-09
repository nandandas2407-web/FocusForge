// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
// PURPOSE: Pomodoro / Focus Timer ("Promodoro") screen with full-bleed wallpaper,
//          liquid glass circular timer ring, ambient focus sounds, and strict lock controls.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AmbientSoundPlayer
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen(
    timerSeconds: Int,
    isRunning: Boolean,
    timerMode: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: (minutes: Int) -> Unit,
    onTick: () -> Unit,
    onSetMode: (mode: String, minutes: Int) -> Unit,
    focusWallpaperOverride: String? = null
) {
    var isStrictModeActive by remember { mutableStateOf(false) }

    // Live countdown effect
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            onTick()
        }
    }

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val totalSeconds = when (timerMode) {
        "SHORT_BREAK" -> 5 * 60
        "LONG_BREAK" -> 15 * 60
        else -> 25 * 60
    }
    val progress = if (totalSeconds > 0) timerSeconds.toFloat() / totalSeconds.toFloat() else 0f

    WallpaperBackground(
        preset = "LOFI_STUDY",
        customWallpaperPath = focusWallpaperOverride,
        darkOverlayAlpha = 0.5f
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pomodoro Focus Timer",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deep work session • App Blocker Active",
                        fontSize = 13.sp,
                        color = GlassTokens.Info
                    )
                }
            }

            // Mode Selector Segmented Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GlassChip(
                        text = "Focus (25m)",
                        isSelected = timerMode == "POMODORO",
                        onClick = { onSetMode("POMODORO", 25) }
                    )
                    GlassChip(
                        text = "Short Break (5m)",
                        isSelected = timerMode == "SHORT_BREAK",
                        onClick = { onSetMode("SHORT_BREAK", 5) }
                    )
                    GlassChip(
                        text = "Long Break (15m)",
                        isSelected = timerMode == "LONG_BREAK",
                        onClick = { onSetMode("LONG_BREAK", 15) }
                    )
                }
            }

            // Glass Circular Progress Ring Timer
            item {
                GlassCard(
                    modifier = Modifier.padding(vertical = 10.dp),
                    cornerRadius = 32.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        GlassRingProgress(
                            progress = progress,
                            timeText = timeFormatted,
                            statusText = if (isRunning) "FOCUSING" else "PAUSED",
                            accentColor = GlassTokens.Accent
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Controls Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onReset(if (timerMode == "SHORT_BREAK") 5 else if (timerMode == "LONG_BREAK") 15 else 25) },
                                modifier = Modifier.testTag("btn_timer_reset")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Timer",
                                    tint = GlassTokens.TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            GlassButton(
                                text = if (isRunning) "Pause Session" else "Start Session",
                                onClick = { if (isRunning) onPause() else onStart() },
                                icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                testTagStr = "btn_timer_start_pause"
                            )
                        }
                    }
                }
            }

            // Ambient Focus Sound Player
            item {
                AmbientSoundPlayer()
            }

            // Strict Lock Mode Toggle Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = GlassTokens.Warning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Strict Mode Lock",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.TextPrimary
                                )
                                Text(
                                    text = "Prevents early session cancellation or app unlocks",
                                    fontSize = 11.sp,
                                    color = GlassTokens.TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isStrictModeActive,
                            onCheckedChange = { isStrictModeActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlassTokens.Warning,
                                checkedTrackColor = GlassTokens.Warning.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
