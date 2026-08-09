// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/Responsive.kt
// PURPOSE: Responsive layout utilities — adaptive sizing, spacing, and grid
//          helpers for phone and tablet form factors.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Responsive {
    /** True when screen width >= 600dp (tablet breakpoint). */
    @Composable
    fun isTablet(): Boolean {
        val config = LocalConfiguration.current
        return config.screenWidthDp >= 600
    }

    /** Max content width — phones get full width, tablets get 960dp max. */
    @Composable
    fun maxWidth(): Dp = if (isTablet()) 960.dp else Dp.Unspecified

    /** Horizontal padding — phones 20dp, tablets 32dp. */
    @Composable
    fun horizontalPadding(): Dp = if (isTablet()) 32.dp else 20.dp

    /** Content vertical padding top. */
    @Composable
    fun topPadding(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Bottom padding for nav bar clearance. */
    @Composable
    fun bottomPadding(): Dp = if (isTablet()) 32.dp else 120.dp

    /** Card corner radius. */
    @Composable
    fun cardCorner(): Dp = if (isTablet()) 16.dp else 16.dp

    /** Section spacing. */
    @Composable
    fun sectionSpacing(): Dp = if (isTablet()) 24.dp else 16.dp

    /** Number of grid columns for quick actions. */
    @Composable
    fun gridColumns(): Int = if (isTablet()) 4 else 1

    /** Number of grid columns for card lists. */
    @Composable
    fun cardGridColumns(): Int = if (isTablet()) 2 else 1
}

/**
 * Wraps content in a BoxWithConstraints and applies responsive horizontal padding + max width.
 */
@Composable
fun ResponsiveScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hp = Responsive.horizontalPadding()
    val tp = Responsive.topPadding()
    val bp = Responsive.bottomPadding()
    val mw = Responsive.maxWidth()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = mw)
                .padding(horizontal = hp)
                .padding(top = tp, bottom = bp)
        ) {
            content()
        }
    }
}
