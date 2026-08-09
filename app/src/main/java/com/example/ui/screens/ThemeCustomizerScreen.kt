// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/ThemeCustomizerScreen.kt
// PURPOSE: Theme Customizer & Settings screen for Liquid Glass design,
//          wallpapers, glass blur & opacity sliders, and permissions check panel.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — tablet-responsive layout with side-by-side preview & controls
// ============================================================

package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    val isTablet = Responsive.isTablet()

    WallpaperBackground(preset = currentPreset) {
        ResponsiveScaffold {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Responsive.sectionSpacing())
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = GlassTokens.Accent,
                            modifier = Modifier.size(if (isTablet) 36.dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Liquid Glass & Theme",
                                fontSize = if (isTablet) 30.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Customize frosted glass refraction, blur, and wallpapers",
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
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("theme_live_preview_card")
                                ) {
                                    Text(
                                        text = "Live Glass Preview",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Adjust the sliders below to fine-tune the ambient backdrop and card transparency.",
                                        fontSize = 14.sp,
                                        color = GlassTokens.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
                                WallpaperPresetSelection(
                                    currentPreset = currentPreset,
                                    onPresetSelected = { preset ->
                                        currentPreset = preset
                                        onUpdateWallpaperPreset(preset)
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                GlassSliderCard(
                                    title = "Glass Blur Intensity: ${blurSigma.toInt()} px",
                                    value = blurSigma,
                                    valueRange = 8f..48f,
                                    onValueChange = {
                                        blurSigma = it
                                        onUpdateGlassBlur(it)
                                    },
                                    trackColor = GlassTokens.Info
                                )
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
                                GlassSliderCard(
                                    title = "Glass Surface Opacity: ${(opacityVal * 100).toInt()}%",
                                    value = opacityVal,
                                    valueRange = 0.05f..0.35f,
                                    onValueChange = {
                                        opacityVal = it
                                        onUpdateGlassOpacity(it)
                                    },
                                    trackColor = GlassTokens.Accent
                                )
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
                                PermissionsPanel()
                            }
                        }
                    }
                } else {
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

                    item {
                        WallpaperPresetSelection(
                            currentPreset = currentPreset,
                            onPresetSelected = { preset ->
                                currentPreset = preset
                                onUpdateWallpaperPreset(preset)
                            }
                        )
                    }

                    item {
                        GlassSliderCard(
                            title = "Glass Blur Intensity: ${blurSigma.toInt()} px",
                            value = blurSigma,
                            valueRange = 8f..48f,
                            onValueChange = {
                                blurSigma = it
                                onUpdateGlassBlur(it)
                            },
                            trackColor = GlassTokens.Info
                        )
                    }

                    item {
                        GlassSliderCard(
                            title = "Glass Surface Opacity: ${(opacityVal * 100).toInt()}%",
                            value = opacityVal,
                            valueRange = 0.05f..0.35f,
                            onValueChange = {
                                opacityVal = it
                                onUpdateGlassOpacity(it)
                            },
                            trackColor = GlassTokens.Accent
                        )
                    }

                    item {
                        PermissionsPanel()
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperPresetSelection(
    currentPreset: String,
    onPresetSelected: (String) -> Unit
) {
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
                onPresetSelected("COSMIC_NEON")
            }
            PresetChip("Lo-Fi Room", currentPreset == "LOFI_STUDY", Modifier.weight(1f)) {
                onPresetSelected("LOFI_STUDY")
            }
            PresetChip("Aurora Glass", currentPreset == "AURORA", Modifier.weight(1f)) {
                onPresetSelected("AURORA")
            }
        }
    }
}

@Composable
private fun GlassSliderCard(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    trackColor: Color
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTokens.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor
            )
        )
    }
}

@Composable
private fun PermissionsPanel() {
    val context = LocalContext.current
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
