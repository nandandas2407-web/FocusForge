// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/StreaksGoalsScreen.kt
// PURPOSE: Focus Streaks & Daily Screen Time Goals screen with database updates.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — tablet-responsive layout with side-by-side streaks & goals
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
    val isTablet = Responsive.isTablet()

    WallpaperBackground(preset = "COSMIC_NEON") {
        ResponsiveScaffold {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Responsive.sectionSpacing())
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = GlassTokens.Warning,
                            modifier = Modifier.size(if (isTablet) 36.dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Streaks & Goals",
                                fontSize = if (isTablet) 30.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Build focus momentum with humane, non-shaming streak tracking",
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                if (isTablet) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Responsive.sectionSpacing())
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                StreakBanner(
                                    currentStreak = currentStreak,
                                    bestStreak = bestStreak
                                )
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
                                GoalSlider(
                                    goalMinutes = goalMinutes,
                                    onGoalChange = {
                                        goalMinutes = it
                                        onUpdateStreakGoal(it.toInt())
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Milestone Badges",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
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
                } else {
                    item {
                        StreakBanner(
                            currentStreak = currentStreak,
                            bestStreak = bestStreak
                        )
                    }

                    item {
                        GoalSlider(
                            goalMinutes = goalMinutes,
                            onGoalChange = {
                                goalMinutes = it
                                onUpdateStreakGoal(it.toInt())
                            }
                        )
                    }

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
}

@Composable
private fun StreakBanner(
    currentStreak: Int,
    bestStreak: Int
) {
    val isTablet = Responsive.isTablet()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Active Streak",
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    color = GlassTokens.TextSecondary
                )
                Text(
                    text = "$currentStreak Days",
                    fontSize = if (isTablet) 42.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.Warning
                )
                Text(
                    text = "Personal Best: $bestStreak Days",
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    color = GlassTokens.TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = GlassTokens.Warning,
                modifier = Modifier.size(if (isTablet) 72.dp else 64.dp)
            )
        }
    }
}

@Composable
private fun GoalSlider(
    goalMinutes: Float,
    onGoalChange: (Float) -> Unit
) {
    val isTablet = Responsive.isTablet()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Daily Screen Time Goal Limit: ${goalMinutes.toInt()} mins",
            fontSize = if (isTablet) 18.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTokens.TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Slide to adjust target daily screen time limit.",
            fontSize = if (isTablet) 14.sp else 12.sp,
            color = GlassTokens.TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Slider(
            value = goalMinutes,
            onValueChange = onGoalChange,
            valueRange = 30f..300f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = GlassTokens.Accent,
                activeTrackColor = GlassTokens.Accent
            )
        )
    }
}

@Composable
private fun BadgeCard(
    title: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val isTablet = Responsive.isTablet()
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) GlassTokens.Warning else GlassTokens.TextMuted,
                modifier = Modifier.size(if (isTablet) 40.dp else 36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) GlassTokens.TextPrimary else GlassTokens.TextMuted
            )
        }
    }
}
