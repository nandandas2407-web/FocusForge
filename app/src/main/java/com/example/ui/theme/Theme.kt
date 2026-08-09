// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/Theme.kt
// PURPOSE: Material 3 Verdant dark theme for FocusForge.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Green minimalism overhaul.
// ============================================================

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlassTokens.Accent,
    secondary = GlassTokens.Info,
    tertiary = GlassTokens.Warning,
    background = GlassTokens.DarkBase,
    surface = GlassTokens.SurfaceDark,
    onPrimary = GlassTokens.DarkBase,
    onSecondary = Color.White,
    onBackground = GlassTokens.TextPrimary,
    onSurface = GlassTokens.TextPrimary,
    error = GlassTokens.Danger,
    onError = Color.White
)

@Composable
fun FocusForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
