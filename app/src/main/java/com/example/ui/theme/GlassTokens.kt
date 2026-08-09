// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/GlassTokens.kt
// PURPOSE: Design tokens — Verdant green minimalism system.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Brutal minimalism overhaul.
// ============================================================

package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object GlassTokens {
    // Base surfaces
    val DarkBase = Color(0xFF0A0F0D)       // Near-black with green undertone
    val SurfaceDark = Color(0xFF111916)     // Card/panel surface
    val SurfaceElevated = Color(0xFF182019) // Elevated elements

    // Glass (subtle, not loud)
    val GlassTintStart = Color(0x0DFFFFFF)
    val GlassTintEnd = Color(0x05FFFFFF)
    val SpecularBorderStart = Color(0x14FFFFFF)
    val SpecularBorderEnd = Color(0x06FFFFFF)

    // Accent palette
    val Accent = Color(0xFF22C55E)          // Primary green
    val AccentMuted = Color(0xFF16A34A)     // Darker green for pressed
    val AccentDim = Color(0xFF15803D)       // Deep green
    val AccentGlow = Color(0x3322C55E)      // Green at 20% for subtle glows

    // Semantic
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Danger = Color(0xFFEF4444)
    val Info = Color(0xFF38BDF8)

    // Text
    val TextPrimary = Color(0xFFE8F0EA)     // Warm white with green tint
    val TextSecondary = Color(0xFF8A9B8E)   // Muted sage
    val TextMuted = Color(0xFF556B59)       // Dim green-gray
}
