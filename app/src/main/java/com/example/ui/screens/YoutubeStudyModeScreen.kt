// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/YoutubeStudyModeScreen.kt
// PURPOSE: Educational YouTube whitelist manager screen for Focus Study Mode
//          with link parsing, toggle on/off, and responsive tablet layout.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.YoutubeWhitelistEntity
import com.example.ui.theme.*

@Composable
fun YoutubeStudyModeScreen(
    whitelist: List<YoutubeWhitelistEntity>,
    isStudyModeEnabled: Boolean = true,
    onToggleStudyMode: (Boolean) -> Unit = {},
    onAddChannel: (channelId: String, channelTitle: String) -> Unit,
    onRemoveChannel: (channel: YoutubeWhitelistEntity) -> Unit
) {
    var channelInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    WallpaperBackground(preset = "COSMIC_NEON") {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth >= 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = if (isTablet) 32.dp else 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Header & Toggle Bar
                GlassCard(modifier = Modifier.fillMaxWidth().testTag("youtube_study_toggle_card")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = GlassTokens.Warning,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "YouTube Study Mode",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.TextPrimary
                                )
                                Text(
                                    text = if (isStudyModeEnabled) "Active: Non-educational content is restricted" else "Disabled: Normal YouTube access",
                                    fontSize = 12.sp,
                                    color = if (isStudyModeEnabled) GlassTokens.Success else GlassTokens.TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isStudyModeEnabled,
                            onCheckedChange = onToggleStudyMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlassTokens.TextPrimary,
                                checkedTrackColor = GlassTokens.Warning,
                                uncheckedThumbColor = GlassTokens.TextSecondary,
                                uncheckedTrackColor = GlassTokens.SurfaceDark
                            ),
                            modifier = Modifier.testTag("toggle_youtube_study_mode")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Educational Channel by Link/Title Form
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = GlassTokens.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Channel Link or Handle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste a YouTube channel URL (preferably an @handle), handle, or exact channel title. Only whitelisted channels can play videos in Study Mode.",
                        fontSize = 12.sp,
                        color = GlassTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        value = channelInput,
                        onValueChange = {
                            channelInput = it
                            inputError = false
                        },
                        placeholder = "Paste YouTube Channel Link or Handle...",
                        testTagStr = "input_youtube_channel_link"
                    )
                    if (inputError) {
                        Text(
                            text = "Please enter a valid YouTube channel URL or name",
                            color = GlassTokens.Danger,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Add Channel to Whitelist",
                        onClick = {
                            val trimmed = channelInput.trim()
                            if (trimmed.isNotBlank()) {
                                val (id, title) = parseChannelInfo(trimmed)
                                onAddChannel(id, title)
                                channelInput = ""
                                inputError = false
                            } else {
                                inputError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Add,
                        accentColor = GlassTokens.Warning,
                        testTagStr = "btn_add_youtube_channel"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Approved Whitelisted Channels (${whitelist.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(whitelist.size) { idx ->
                            ChannelCardItem(
                                channel = whitelist[idx],
                                onRemoveChannel = onRemoveChannel
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(whitelist.size) { idx ->
                            ChannelCardItem(
                                channel = whitelist[idx],
                                onRemoveChannel = onRemoveChannel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCardItem(
    channel: YoutubeWhitelistEntity,
    onRemoveChannel: (channel: YoutubeWhitelistEntity) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.channelTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTokens.TextPrimary
                )
                Text(
                    text = "ID / Handle: ${channel.channelId}",
                    fontSize = 12.sp,
                    color = GlassTokens.Warning
                )
            }

            IconButton(
                onClick = { onRemoveChannel(channel) },
                modifier = Modifier.testTag("btn_delete_channel_${channel.channelId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Channel",
                    tint = GlassTokens.TextSecondary
                )
            }
        }
    }
}

private fun parseChannelInfo(input: String): Pair<String, String> {
    val trimmed = input.trim()
    return when {
        trimmed.contains("youtube.com/@") -> {
            val handle = "@" + trimmed.substringAfter("youtube.com/@").substringBefore("/").substringBefore("?")
            Pair(handle, handle)
        }
        trimmed.contains("youtube.com/channel/") -> {
            val channelId = trimmed.substringAfter("youtube.com/channel/").substringBefore("/").substringBefore("?")
            Pair(channelId, "Channel ($channelId)")
        }
        trimmed.contains("youtube.com/c/") -> {
            val channelName = trimmed.substringAfter("youtube.com/c/").substringBefore("/").substringBefore("?")
            Pair(channelName, channelName)
        }
        trimmed.startsWith("@") -> {
            Pair(trimmed, trimmed)
        }
        else -> {
            Pair(trimmed, trimmed)
        }
    }
}
