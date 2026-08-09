// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/WallpaperBackground.kt
// PURPOSE: Full-bleed procedural ambient wallpaper canvas with dark liquid glass
//          refraction, aura glows, and custom image file support for Pomodoro/Home screens.
// CREATED: 2026-08-09
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
    accentColor: Color = GlassTokens.ElectricViolet,
    darkOverlayAlpha: Float = 0.45f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(GlassTokens.DarkBase)) {

        if (!customWallpaperPath.isNullOrEmpty() && File(customWallpaperPath).exists()) {
            AsyncImage(
                model = File(customWallpaperPath),
                contentDescription = "Custom Wallpaper",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Procedural ambient liquid glass backdrop
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (preset) {
                    "LOFI_STUDY" -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF2A1B4E), Color.Transparent),
                                center = Offset(width * 0.2f, height * 0.3f),
                                radius = width * 0.8f
                            ),
                            radius = width * 0.8f,
                            center = Offset(width * 0.2f, height * 0.3f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0D3B66), Color.Transparent),
                                center = Offset(width * 0.8f, height * 0.7f),
                                radius = width * 0.9f
                            ),
                            radius = width * 0.9f,
                            center = Offset(width * 0.8f, height * 0.7f)
                        )
                    }
                    "AURORA" -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF003B46), Color.Transparent),
                                center = Offset(width * 0.5f, height * 0.2f),
                                radius = width * 0.9f
                            ),
                            radius = width * 0.9f,
                            center = Offset(width * 0.5f, height * 0.2f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF07575B), Color.Transparent),
                                center = Offset(width * 0.8f, height * 0.6f),
                                radius = width * 0.7f
                            ),
                            radius = width * 0.7f,
                            center = Offset(width * 0.8f, height * 0.6f)
                        )
                    }
                    else -> { // COSMIC_NEON
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(width * 0.3f, height * 0.25f),
                                radius = width * 0.85f
                            ),
                            radius = width * 0.85f,
                            center = Offset(width * 0.3f, height * 0.25f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GlassTokens.NeonCyan.copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(width * 0.75f, height * 0.7f),
                                radius = width * 0.8f
                            ),
                            radius = width * 0.8f,
                            center = Offset(width * 0.75f, height * 0.7f)
                        )
                    }
                }
            }
        }

        // Dark refraction overlay for content legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = darkOverlayAlpha))
        )

        content()
    }
}
