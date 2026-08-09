// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/ShortsReelsBlockerScreen.kt
// PURPOSE: Granular Reels & Shorts sub-screen blocking manager with live counters
//          and tablet-optimized layout.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.VideoLibrary
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
fun ShortsReelsBlockerScreen(
    blockedApps: List<BlockedAppEntity>,
    onToggleReelsBlocked: (packageName: String, appName: String, isBlocked: Boolean) -> Unit,
    onToggleShortsBlocked: (packageName: String, appName: String, isBlocked: Boolean) -> Unit
) {
    val instagram = blockedApps.find { it.packageName == "com.instagram.android" }
    val isReelsBlocked = instagram?.isReelsBlocked ?: true
    val reelsBlockedCount = instagram?.timesBlockedToday ?: 0

    val youtube = blockedApps.find { it.packageName == "com.google.android.youtube" }
    val isShortsBlocked = youtube?.isShortsBlocked ?: true
    val shortsBlockedCount = youtube?.timesBlockedToday ?: 0

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
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = GlassTokens.NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Shorts & Reels Blocker",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Block addictive short-form video feeds while keeping main app functionality",
                                fontSize = 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                // Instagram Reels Card
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_block_reels")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = GlassTokens.ElectricViolet,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Instagram Reels",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Blocks Reels tab while allowing DMs & posts",
                                        fontSize = 12.sp,
                                        color = GlassTokens.TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isReelsBlocked,
                                onCheckedChange = { checked ->
                                    onToggleReelsBlocked("com.instagram.android", "Instagram", checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GlassTokens.ElectricViolet,
                                    checkedTrackColor = GlassTokens.ElectricViolet.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (reelsBlockedCount > 0) "$reelsBlockedCount Reels loops intercepted today" else "0 Reels attempts blocked today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTokens.VibrantGreen
                        )
                    }
                }

                // YouTube Shorts Card
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_block_shorts")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = GlassTokens.SoftRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "YouTube Shorts",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Intercepts Shorts player while keeping long-form videos",
                                        fontSize = 12.sp,
                                        color = GlassTokens.TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isShortsBlocked,
                                onCheckedChange = { checked ->
                                    onToggleShortsBlocked("com.google.android.youtube", "YouTube", checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GlassTokens.SoftRed,
                                    checkedTrackColor = GlassTokens.SoftRed.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (shortsBlockedCount > 0) "$shortsBlockedCount Shorts attempts blocked today" else "0 Shorts attempts blocked today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTokens.VibrantGreen
                        )
                    }
                }
            }
        }
    }
}
