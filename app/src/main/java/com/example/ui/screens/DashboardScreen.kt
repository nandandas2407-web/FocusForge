// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/DashboardScreen.kt
// PURPOSE: Main Home Dashboard with Liquid Glass hero card, dynamic usage statistics,
//          real focus session goals, quick action row, and today's schedule timeline.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.StreakGoalEntity
import com.example.data.entity.TaskEntity
import com.example.data.repository.AppUsageInfo
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToTimer: () -> Unit,
    onNavigateToBlocker: () -> Unit,
    onNavigateToShorts: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToYoutube: () -> Unit,
    tasks: List<TaskEntity>,
    focusSessions: List<FocusSessionEntity> = emptyList(),
    streakGoal: StreakGoalEntity? = null,
    usageStats: List<AppUsageInfo> = emptyList()
) {
    // Calculate real foreground usage today
    val totalUsageMs = usageStats.sumOf { it.totalTimeInForegroundMs }
    val usageHours = (totalUsageMs / 3600000).toInt()
    val usageMinutes = ((totalUsageMs % 3600000) / 60000).toInt()

    // Calculate real focus minutes logged today
    val todayFocusMinutes = focusSessions.filter { it.isCompleted }.sumOf { it.durationMinutes }

    // Real streak calculation
    val streakDays = streakGoal?.currentStreakDays ?: 1
    val dailyGoalMinutes = streakGoal?.dailyScreenTimeGoalMinutes ?: 120
    val totalScreenTimeMinutes = (totalUsageMs / 60000).toInt()
    
    val progressFraction = if (dailyGoalMinutes > 0) {
        (totalScreenTimeMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0.5f

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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FocusForge",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Stay focused • Protect your time",
                                fontSize = 13.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }

                        // Streak Flame Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GlassTokens.Warning.copy(alpha = 0.2f),
                            modifier = Modifier.border(1.dp, GlassTokens.Warning, RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = GlassTokens.Warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$streakDays Day Streak",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.Warning
                                )
                            }
                        }
                    }
                }

                // Hero Card: Real Screen Time & Real Goal Progress
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Today's Screen Time",
                                    fontSize = 14.sp,
                                    color = GlassTokens.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (totalUsageMs > 0) "${usageHours}h ${usageMinutes}m" else "0m tracked",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = GlassTokens.Success,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$todayFocusMinutes mins focused today",
                                        fontSize = 12.sp,
                                        color = GlassTokens.Success,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Circular progress indicator based on real goal
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                                CircularProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxSize(),
                                    color = GlassTokens.Accent,
                                    trackColor = Color.White.copy(alpha = 0.15f),
                                    strokeWidth = 10.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Limit",
                                        fontSize = 10.sp,
                                        color = GlassTokens.TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GlassButton(
                            text = "Start Focus Session",
                            onClick = onNavigateToTimer,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.PlayArrow,
                            testTagStr = "hero_start_focus_button"
                        )
                    }
                }

                // Quick Actions Strip
                item {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            ActionChip(
                                title = "App Blocker",
                                icon = Icons.Default.Block,
                                accent = GlassTokens.Accent,
                                onClick = onNavigateToBlocker,
                                testTagStr = "chip_app_blocker"
                            )
                        }
                        item {
                            ActionChip(
                                title = "Reels & Shorts",
                                icon = Icons.Default.MovieFilter,
                                accent = GlassTokens.Info,
                                onClick = onNavigateToShorts,
                                testTagStr = "chip_reels_shorts"
                            )
                        }
                        item {
                            ActionChip(
                                title = "Study YouTube",
                                icon = Icons.Default.School,
                                accent = GlassTokens.Warning,
                                onClick = onNavigateToYoutube,
                                testTagStr = "chip_study_youtube"
                            )
                        }
                        item {
                            ActionChip(
                                title = "Add Task",
                                icon = Icons.Default.Add,
                                accent = GlassTokens.Success,
                                onClick = onNavigateToTasks,
                                testTagStr = "chip_add_task"
                            )
                        }
                    }
                }

                // Today's Study Tasks Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Study Tasks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            color = GlassTokens.Accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToTasks() }
                        )
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No study tasks scheduled for today. Tap 'Add Task' to create one!",
                                fontSize = 13.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                } else {
                    items(tasks.size) { index ->
                        val task = tasks[index]
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) GlassTokens.Success else GlassTokens.TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    if (task.notes.isNotEmpty()) {
                                        Text(
                                            text = task.notes,
                                            fontSize = 12.sp,
                                            color = GlassTokens.TextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GlassTokens.Accent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = task.priority,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.Accent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
private fun ActionChip(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTagStr: String
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .testTag(testTagStr)
            .clip(shape)
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.1f),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTokens.TextPrimary
            )
        }
    }
}
