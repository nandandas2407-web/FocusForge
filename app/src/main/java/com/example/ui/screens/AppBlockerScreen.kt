// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/AppBlockerScreen.kt
// PURPOSE: App Blocker management screen with live search, category filter chips,
//          and quick toggle switches for full app locks.
//          Tablet layout uses 2-column grid via chunked rows;
//          phone uses single LazyColumn.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BlockedAppEntity
import com.example.ui.theme.*

@Composable
fun AppBlockerScreen(
    blockedApps: List<BlockedAppEntity>,
    onToggleAppBlocked: (packageName: String, appName: String, category: String, isBlocked: Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val defaultAppList = remember {
        listOf(
            BlockedAppEntity("com.instagram.android", "Instagram", "Social"),
            BlockedAppEntity("com.google.android.youtube", "YouTube", "Video"),
            BlockedAppEntity("com.zhiliaoapp.musically", "TikTok", "Video"),
            BlockedAppEntity("com.snapchat.android", "Snapchat", "Social"),
            BlockedAppEntity("com.facebook.katana", "Facebook", "Social"),
            BlockedAppEntity("com.twitter.android", "X / Twitter", "Social"),
            BlockedAppEntity("com.reddit.frontpage", "Reddit", "Social"),
            BlockedAppEntity("com.supercell.clashofclans", "Clash of Clans", "Games"),
            BlockedAppEntity("com.amazon.mShop.android.shopping", "Amazon Shopping", "Shopping")
        )
    }

    val categories = listOf("All", "Social", "Video", "Games", "Shopping", "System")

    val filteredList = remember(searchQuery, selectedCategory, blockedApps) {
        val mergedMap = defaultAppList.associateBy { it.packageName }.toMutableMap()
        blockedApps.forEach { mergedMap[it.packageName] = it }

        mergedMap.values
            .sortedBy { it.appName.lowercase() }
            .filter { app ->
                val matchesCategory = (selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true))
                val matchesSearch = searchQuery.isEmpty() || app.appName.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
    }

    val isTablet = Responsive.isTablet()
    val sectionSpacing = Responsive.sectionSpacing()
    val appItemModifier: Modifier = Modifier.fillMaxWidth()

    WallpaperBackground(preset = "COSMIC_NEON") {
        ResponsiveScaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                // Header
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = GlassTokens.Accent,
                            modifier = Modifier.size(if (isTablet) 36.dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Blocker",
                                fontSize = if (isTablet) 30.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Choose applications to restrict during focus sessions",
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                // Search Bar
                item {
                    GlassTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search installed applications...",
                        testTagStr = "app_blocker_search_input"
                    )
                }

                // Category Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories.size) { idx ->
                            val cat = categories[idx]
                            GlassChip(
                                text = cat,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }

                // App List Items
                if (isTablet) {
                    // Tablet: 2-column grid via chunked rows
                    val chunked = filteredList.chunked(2)
                    items(chunked.size) { rowIdx ->
                        val row = chunked[rowIdx]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { app ->
                                val isBlocked = app.isFullyBlocked
                                GlassCard(
                                    modifier = appItemModifier
                                        .weight(1f)
                                        .testTag("app_item_${app.packageName}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.appName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = GlassTokens.TextPrimary
                                            )
                                            Text(
                                                text = "${app.category} • ${if (isBlocked) "Locked in Focus Mode" else "Allowed"}",
                                                fontSize = 12.sp,
                                                color = if (isBlocked) GlassTokens.Danger else GlassTokens.TextSecondary
                                            )
                                        }

                                        Switch(
                                            checked = isBlocked,
                                            onCheckedChange = { checked ->
                                                onToggleAppBlocked(app.packageName, app.appName, app.category, checked)
                                            },
                                            thumbContent = {
                                                Icon(
                                                    imageVector = if (isBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = GlassTokens.Danger,
                                                checkedTrackColor = GlassTokens.Danger.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if odd number of items
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    // Phone: single-column list
                    items(filteredList.size) { idx ->
                        val app = filteredList[idx]
                        val isBlocked = app.isFullyBlocked

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_item_${app.packageName}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "${app.category} • ${if (isBlocked) "Locked in Focus Mode" else "Allowed"}",
                                        fontSize = 12.sp,
                                        color = if (isBlocked) GlassTokens.Danger else GlassTokens.TextSecondary
                                    )
                                }

                                Switch(
                                    checked = isBlocked,
                                    onCheckedChange = { checked ->
                                        onToggleAppBlocked(app.packageName, app.appName, app.category, checked)
                                    },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (isBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GlassTokens.Danger,
                                        checkedTrackColor = GlassTokens.Danger.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
