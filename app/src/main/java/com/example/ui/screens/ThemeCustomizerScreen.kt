// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/ThemeCustomizerScreen.kt
// PURPOSE: Theme Customizer & Settings screen for Liquid Glass design,
//          wallpapers, glass blur & opacity sliders, and permissions check panel.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ThemeSettingsEntity
import com.example.ui.theme.*

@Composable
fun ThemeCustomizerScreen(
    themeSettings: ThemeSettingsEntity?,
    onUpdateWallpaperPreset: (preset: String) -> Unit,
    onUpdateGlassBlur: (blur: Float) -> Unit,
    onUpdateGlassOpacity: (opacity: Float) -> Unit
) {
    val context = LocalContext.current
    var currentPreset by remember { mutableStateOf(themeSettings?.wallpaperPreset ?: "COSMIC_NEON") }
    var blurSigma by remember { mutableFloatStateOf(themeSettings?.glassBlurSigma ?: 24f) }
    var opacityVal by remember { mutableFloatStateOf(themeSettings?.glassOpacity ?: 0.15f) }

    WallpaperBackground(preset = currentPreset) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = GlassTokens.Accent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Liquid Glass & Theme",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                        Text(
                            text = "Customize frosted glass refraction, blur, and wallpapers",
                            fontSize = 12.sp,
                            color = GlassTokens.TextSecondary
                        )
                    }
                }
            }

            // Live Preview Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("theme_live_preview_card")
                ) {
                    Text(
                        text = "Live Glass Preview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adjust the sliders below to fine-tune the ambient backdrop and card transparency.",
                        fontSize = 13.sp,
                        color = GlassTokens.TextSecondary
                    )
                }
            }

            // Wallpaper Preset Selection
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ambient Wallpaper Backdrop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PresetChip("Cosmic Neon", currentPreset == "COSMIC_NEON", Modifier.weight(1f)) {
                            currentPreset = "COSMIC_NEON"
                            onUpdateWallpaperPreset("COSMIC_NEON")
                        }
                        PresetChip("Lo-Fi Room", currentPreset == "LOFI_STUDY", Modifier.weight(1f)) {
                            currentPreset = "LOFI_STUDY"
                            onUpdateWallpaperPreset("LOFI_STUDY")
                        }
                        PresetChip("Aurora Glass", currentPreset == "AURORA", Modifier.weight(1f)) {
                            currentPreset = "AURORA"
                            onUpdateWallpaperPreset("AURORA")
                        }
                    }
                }
            }

            // Glass Refraction Blur Slider
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Glass Blur Intensity: ${blurSigma.toInt()} px",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = blurSigma,
                        onValueChange = {
                            blurSigma = it
                            onUpdateGlassBlur(it)
                        },
                        valueRange = 8f..48f,
                        colors = SliderDefaults.colors(
                            thumbColor = GlassTokens.Info,
                            activeTrackColor = GlassTokens.Info
                        )
                    )
                }
            }

            // Glass Opacity Slider
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Glass Surface Opacity: ${(opacityVal * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = opacityVal,
                        onValueChange = {
                            opacityVal = it
                            onUpdateGlassOpacity(it)
                        },
                        valueRange = 0.05f..0.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = GlassTokens.Accent,
                            activeTrackColor = GlassTokens.Accent
                        )
                    )
                }
            }

            // System Permissions Settings Panel
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GlassTokens.Info,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Permissions Panel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "One-tap shortcuts to Android Settings",
                                fontSize = 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassTokens.Accent)
                        ) {
                            Text("Open", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) GlassTokens.Accent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (isSelected) GlassTokens.Accent else Color.White.copy(alpha = 0.15f), shape)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextSecondary
        )
    }
}
