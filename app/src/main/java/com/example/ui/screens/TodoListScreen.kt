// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/TodoListScreen.kt
// PURPOSE: Study To-Do List screen with priorities, categories, completion toggles,
//          and task creator modal.
// UPDATED: 2026-08-09 — tablet-responsive layout
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.example.data.entity.TaskEntity
import com.example.ui.theme.*

@Composable
fun TodoListScreen(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, notes: String, priority: String, category: String, estimatedMinutes: Int) -> Unit,
    onToggleTask: (task: TaskEntity) -> Unit,
    onDeleteTask: (id: Long) -> Unit,
    onStartTaskSession: (task: TaskEntity) -> Unit
) {
    var showAddModal by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf("MEDIUM") }
    var newCategory by remember { mutableStateOf("Study") }

    val isTablet = Responsive.isTablet()
    val hPadding = Responsive.horizontalPadding()
    val sectionSpacing = Responsive.sectionSpacing()
    val titleFontSize = if (isTablet) 30.sp else 26.sp
    val subtitleFontSize = if (isTablet) 14.sp else 12.sp

    WallpaperBackground(preset = "COSMIC_NEON") {
        ResponsiveScaffold {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("task_grid_tablet"),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            TabletTodoHeader(
                                titleFontSize = titleFontSize,
                                subtitleFontSize = subtitleFontSize,
                                onAddClick = { showAddModal = true }
                            )
                        }
                        items(tasks.size) { idx ->
                            val task = tasks[idx]
                            TaskCard(
                                task = task,
                                onToggleTask = onToggleTask,
                                onDeleteTask = onDeleteTask,
                                onStartTaskSession = onStartTaskSession
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = hPadding),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                    ) {
                        item {
                            PhoneTodoHeader(
                                titleFontSize = titleFontSize,
                                subtitleFontSize = subtitleFontSize,
                                onAddClick = { showAddModal = true }
                            )
                        }
                        items(tasks.size) { idx ->
                            val task = tasks[idx]
                            TaskCard(
                                task = task,
                                onToggleTask = onToggleTask,
                                onDeleteTask = onDeleteTask,
                                onStartTaskSession = onStartTaskSession
                            )
                        }
                    }
                }

                // Create Task Dialog
                if (showAddModal) {
                    AlertDialog(
                        onDismissRequest = { showAddModal = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        onAddTask(newTitle, newNotes, newPriority, newCategory, 25)
                                        newTitle = ""
                                        newNotes = ""
                                        showAddModal = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassTokens.Accent)
                            ) {
                                Text("Create Task")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddModal = false }) {
                                Text("Cancel", color = GlassTokens.TextSecondary)
                            }
                        },
                        title = {
                            Text("Create New Task", color = GlassTokens.TextPrimary, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                GlassTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    placeholder = "Task title (e.g. Study Math Ch. 2)...",
                                    testTagStr = "input_task_title"
                                )
                                GlassTextField(
                                    value = newNotes,
                                    onValueChange = { newNotes = it },
                                    placeholder = "Optional notes or problem numbers...",
                                    testTagStr = "input_task_notes"
                                )
                            }
                        },
                        containerColor = GlassTokens.SurfaceDark
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletTodoHeader(
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    subtitleFontSize: androidx.compose.ui.unit.TextUnit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Study Tasks & To-Do",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.TextPrimary
            )
            Text(
                text = "Organize goals and link tasks to Pomodoro sessions",
                fontSize = subtitleFontSize,
                color = GlassTokens.TextSecondary
            )
        }
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = GlassTokens.Accent,
            contentColor = GlassTokens.TextPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("fab_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}

@Composable
private fun PhoneTodoHeader(
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    subtitleFontSize: androidx.compose.ui.unit.TextUnit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Study Tasks & To-Do",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.TextPrimary
            )
            Text(
                text = "Organize goals and link tasks to Pomodoro sessions",
                fontSize = subtitleFontSize,
                color = GlassTokens.TextSecondary
            )
        }
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = GlassTokens.Accent,
            contentColor = GlassTokens.TextPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("fab_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (id: Long) -> Unit,
    onStartTaskSession: (TaskEntity) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onToggleTask(task) }) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (task.isCompleted) GlassTokens.Success else GlassTokens.TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) GlassTokens.TextMuted else GlassTokens.TextPrimary
                )
                if (task.notes.isNotEmpty()) {
                    Text(
                        text = task.notes,
                        fontSize = 12.sp,
                        color = GlassTokens.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Priority: ${task.priority}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.priority) {
                            "HIGH" -> GlassTokens.Danger
                            "LOW" -> GlassTokens.Success
                            else -> GlassTokens.Warning
                        }
                    )
                    Text(
                        text = "Est: ${task.estimatedMinutes}m",
                        fontSize = 10.sp,
                        color = GlassTokens.Info
                    )
                }
            }

            IconButton(onClick = { onStartTaskSession(task) }) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Focus Task",
                    tint = GlassTokens.Info
                )
            }

            IconButton(onClick = { onDeleteTask(task.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = GlassTokens.TextMuted
                )
            }
        }
    }
}
