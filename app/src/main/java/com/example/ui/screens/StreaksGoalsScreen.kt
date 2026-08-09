// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/StreaksGoalsScreen.kt
// PURPOSE: Focus Streaks & Daily Screen Time Goals screen with database updates.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StreakGoalEntity
import com.example.ui.theme.*

@Composable
fun StreaksGoalsScreen(
    streakGoal: StreakGoalEntity?,
    onUpdateStreakGoal: (goalMinutes: Int) -> Unit = {}
) {
    val currentStreak = streakGoal?.currentStreakDays ?: 1
    val bestStreak = streakGoal?.bestStreakDays ?: 7
    var goalMinutes by remember(streakGoal?.dailyScreenTimeGoalMinutes) {
        mutableFloatStateOf(streakGoal?.dailyScreenTimeGoalMinutes?.toFloat() ?: 120f)
    }

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
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = GlassTokens.WarmGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Streaks & Goals",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Build focus momentum with humane, non-shaming streak tracking",
                                fontSize = 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                // Flame Streak Banner
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Active Streak",
                                    fontSize = 14.sp,
                                    color = GlassTokens.TextSecondary
                                )
                                Text(
                                    text = "$currentStreak Days",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.WarmGold
                                )
                                Text(
                                    text = "Personal Best: $bestStreak Days",
                                    fontSize = 12.sp,
                                    color = GlassTokens.TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GlassTokens.WarmGold,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }

                // Daily Screen Time Goal Slider
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Daily Screen Time Goal Limit: ${goalMinutes.toInt()} mins",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Slide to adjust target daily screen time limit.",
                            fontSize = 12.sp,
                            color = GlassTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = goalMinutes,
                            onValueChange = {
                                goalMinutes = it
                                onUpdateStreakGoal(it.toInt())
                            },
                            valueRange = 30f..300f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = GlassTokens.ElectricViolet,
                                activeTrackColor = GlassTokens.ElectricViolet
                            )
                        )
                    }
                }

                // Milestone Badges
                item {
                    Text(
                        text = "Milestone Badges",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BadgeCard(
                            title = "3-Day Ignition",
                            isUnlocked = currentStreak >= 3,
                            icon = Icons.Default.MilitaryTech,
                            modifier = Modifier.weight(1f)
                        )
                        BadgeCard(
                            title = "7-Day Focus Master",
                            isUnlocked = currentStreak >= 7,
                            icon = Icons.Default.EmojiEvents,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(
    title: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) GlassTokens.WarmGold else GlassTokens.TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) GlassTokens.TextPrimary else GlassTokens.TextMuted
            )
        }
    }
}
