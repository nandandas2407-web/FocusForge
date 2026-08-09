// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/WallpaperBackground.kt
// PURPOSE: Minimal dark ambient background with subtle green glow.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Brutal minimalism overhaul.
// ============================================================

package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import java.io.File

@Composable
fun WallpaperBackground(
    preset: String = "COSMIC_NEON",
    customWallpaperPath: String? = null,
    accentColor: Color = GlassTokens.Accent,
    darkOverlayAlpha: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(GlassTokens.DarkBase)) {

        if (!customWallpaperPath.isNullOrEmpty() && File(customWallpaperPath).exists()) {
            AsyncImage(
                model = File(customWallpaperPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                when (preset) {
                    "LOFI_STUDY" -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0D1F12), Color.Transparent),
                                center = Offset(w * 0.2f, h * 0.3f),
                                radius = w * 0.8f
                            ),
                            radius = w * 0.8f,
                            center = Offset(w * 0.2f, h * 0.3f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0A1A0E), Color.Transparent),
                                center = Offset(w * 0.8f, h * 0.7f),
                                radius = w * 0.9f
                            ),
                            radius = w * 0.9f,
                            center = Offset(w * 0.8f, h * 0.7f)
                        )
                    }
                    "AURORA" -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0A1F14), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.2f),
                                radius = w * 0.9f
                            ),
                            radius = w * 0.9f,
                            center = Offset(w * 0.5f, h * 0.2f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF071A0F), Color.Transparent),
                                center = Offset(w * 0.8f, h * 0.6f),
                                radius = w * 0.7f
                            ),
                            radius = w * 0.7f,
                            center = Offset(w * 0.8f, h * 0.6f)
                        )
                    }
                    else -> { // COSMIC_NEON — minimal green glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(w * 0.3f, h * 0.25f),
                                radius = w * 0.85f
                            ),
                            radius = w * 0.85f,
                            center = Offset(w * 0.3f, h * 0.25f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.04f), Color.Transparent),
                                center = Offset(w * 0.75f, h * 0.7f),
                                radius = w * 0.8f
                            ),
                            radius = w * 0.8f,
                            center = Offset(w * 0.75f, h * 0.7f)
                        )
                    }
                }
            }
        }

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = darkOverlayAlpha))
        )

        content()
    }
}
