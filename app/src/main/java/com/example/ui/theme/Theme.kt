// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/Theme.kt
// PURPOSE: Material 3 Liquid Glass theme wrapper for FocusForge.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlassTokens.ElectricViolet,
    secondary = GlassTokens.NeonCyan,
    tertiary = GlassTokens.WarmGold,
    background = GlassTokens.DarkBase,
    surface = GlassTokens.SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = GlassTokens.TextPrimary,
    onSurface = GlassTokens.TextPrimary
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
