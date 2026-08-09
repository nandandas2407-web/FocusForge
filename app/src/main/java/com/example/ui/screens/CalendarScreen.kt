// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/CalendarScreen.kt
// PURPOSE: Calendar & Study Schedule screen with exam countdown cards.
// UPDATED: 2026-08-09 — tablet-responsive layout
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CalendarEventEntity
import com.example.ui.theme.*

@Composable
fun CalendarScreen(
    events: List<CalendarEventEntity>,
    onAddEvent: (title: String, eventType: String, dateString: String, isExam: Boolean) -> Unit,
    onDeleteEvent: (id: Long) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var isExamChecked by remember { mutableStateOf(false) }

    val examCountdowns = events.filter { it.isExamCountdown }
    val regularEvents = events.filter { !it.isExamCountdown }

    val isTablet = Responsive.isTablet()
    val sectionSpacing = Responsive.sectionSpacing()
    val titleFontSize = if (isTablet) 30.sp else 26.sp
    val subtitleFontSize = if (isTablet) 14.sp else 12.sp

    WallpaperBackground(preset = "COSMIC_NEON") {
        if (isTablet) {
            ResponsiveScaffold {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        CalendarHeader(
                            titleFontSize = titleFontSize,
                            subtitleFontSize = subtitleFontSize,
                            onAddClick = { showModal = true }
                        )
                    }

                    if (examCountdowns.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Exam Countdowns",
                                fontSize = if (isTablet) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                        }
                        items(examCountdowns.size) { idx ->
                            ExamCountdownCard(exam = examCountdowns[idx])
                        }
                    }

                    if (regularEvents.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Scheduled Study Events",
                                fontSize = if (isTablet) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                        }
                        items(regularEvents.size) { idx ->
                            EventCard(event = regularEvents[idx])
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Responsive.horizontalPadding()),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                item {
                    CalendarHeader(
                        titleFontSize = titleFontSize,
                        subtitleFontSize = subtitleFontSize,
                        onAddClick = { showModal = true }
                    )
                }

                if (examCountdowns.isNotEmpty()) {
                    item {
                        Text(
                            text = "Exam Countdowns",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                    }
                    items(examCountdowns.size) { idx ->
                        ExamCountdownCard(exam = examCountdowns[idx])
                    }
                }

                item {
                    Text(
                        text = "Scheduled Study Events",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                }

                items(events.size) { idx ->
                    EventCard(event = events[idx])
                }
            }
        }

        if (showModal) {
            AlertDialog(
                onDismissRequest = { showModal = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (titleInput.isNotBlank()) {
                                onAddEvent(titleInput, if (isExamChecked) "EXAM" else "STUDY_BLOCK", "2026-08-14", isExamChecked)
                                titleInput = ""
                                showModal = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassTokens.Accent)
                    ) {
                        Text("Add Event")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showModal = false }) {
                        Text("Cancel", color = GlassTokens.TextSecondary)
                    }
                },
                title = { Text("Schedule Event", color = GlassTokens.TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            placeholder = "Event title (e.g. Physics Midterm)...",
                            testTagStr = "input_calendar_title"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isExamChecked,
                                onCheckedChange = { isExamChecked = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Exam Countdown", color = GlassTokens.TextPrimary)
                        }
                    }
                },
                containerColor = GlassTokens.SurfaceDark
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    subtitleFontSize: androidx.compose.ui.unit.TextUnit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = GlassTokens.Info,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Study Calendar",
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.TextPrimary
                )
                Text(
                    text = "Class schedules, exam countdowns & study blocks",
                    fontSize = subtitleFontSize,
                    color = GlassTokens.TextSecondary
                )
            }
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier.testTag("btn_add_calendar_event")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event",
                tint = GlassTokens.Info
            )
        }
    }
}

@Composable
private fun ExamCountdownCard(exam: CalendarEventEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = GlassTokens.Danger,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = exam.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Text(
                        text = "Date: ${exam.dateString}",
                        fontSize = 12.sp,
                        color = GlassTokens.TextSecondary
                    )
                }
            }

            Text(
                text = "5 Days Left",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.Danger
            )
        }
    }
}

@Composable
private fun EventCard(event: CalendarEventEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = GlassTokens.Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Text(
                        text = "${event.dateString} • ${event.eventType}",
                        fontSize = 12.sp,
                        color = GlassTokens.TextSecondary
                    )
                }
            }
        }
    }
}
