// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/GlassComponents.kt
// PURPOSE: Minimal green UI components — flat surfaces, clean geometry, no excess.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Brutal minimalism overhaul.
// ============================================================

package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Card ──────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    opacity: Float = 0.06f,
    borderColor: Color = GlassTokens.SpecularBorderStart,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    Column(
        modifier = modifier
            .clip(shape)
            .background(GlassTokens.SurfaceDark.copy(alpha = 0.8f))
            .border(1.dp, borderColor, shape)
            .then(clickableModifier)
            .padding(16.dp),
        content = content
    )
}

// ── Button ────────────────────────────────────────────────────────────

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = GlassTokens.Accent,
    enabled: Boolean = true,
    testTagStr: String = "glass_button"
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .testTag(testTagStr)
            .clip(shape)
            .clickable(enabled = enabled) { onClick() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (enabled) accentColor else Color.Gray.copy(alpha = 0.25f),
                    shape = shape
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) GlassTokens.DarkBase else GlassTokens.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) GlassTokens.DarkBase else GlassTokens.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Text Field ────────────────────────────────────────────────────────

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = Icons.Default.Search,
    testTagStr: String = "glass_input"
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .testTag(testTagStr)
            .fillMaxWidth()
            .clip(shape)
            .background(GlassTokens.SurfaceDark)
            .border(1.dp, GlassTokens.SpecularBorderStart, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = GlassTokens.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = GlassTokens.TextMuted, fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = GlassTokens.TextPrimary,
                    unfocusedTextColor = GlassTokens.TextPrimary
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = GlassTokens.TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
    }
}

// ── Chip ──────────────────────────────────────────────────────────────

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.Accent
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isSelected) accentColor.copy(alpha = 0.15f)
                else GlassTokens.SurfaceDark
            )
            .border(
                1.dp,
                if (isSelected) accentColor.copy(alpha = 0.5f) else GlassTokens.SpecularBorderStart,
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) accentColor else GlassTokens.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Ring Progress ─────────────────────────────────────────────────────

@Composable
fun GlassRingProgress(
    progress: Float,
    timeText: String,
    statusText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.Accent
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background track
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.TextPrimary,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = GlassTokens.TextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ── Navigation ────────────────────────────────────────────────────────

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun GlassBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    items: List<NavigationItem>,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.Accent
) {
    val shape = RoundedCornerShape(0.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassTokens.SurfaceDark)
            .border(1.dp, GlassTokens.SpecularBorderStart, shape)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                Box(
                    modifier = Modifier
                        .testTag("nav_${item.route}")
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) accentColor else GlassTokens.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) accentColor else GlassTokens.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassNavRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    items: List<NavigationItem>,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.Accent
) {
    val shape = RoundedCornerShape(0.dp)

    Box(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(GlassTokens.SurfaceDark)
            .border(1.dp, GlassTokens.SpecularBorderStart, shape)
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                Box(
                    modifier = Modifier
                        .testTag("nav_rail_${item.route}")
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) accentColor else GlassTokens.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.title,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) accentColor else GlassTokens.TextMuted
                        )
                    }
                }
            }
        }
    }
}
