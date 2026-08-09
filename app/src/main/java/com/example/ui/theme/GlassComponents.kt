// ============================================================
// FILE: app/src/main/java/com/example/ui/theme/GlassComponents.kt
// PURPOSE: Reusable Liquid Glass UI components (GlassCard, GlassButton, GlassBottomNav,
//          GlassTextField, GlassChip, GlassRingProgress).
// CREATED: 2026-08-09
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    opacity: Float = 0.14f,
    borderColor: Color = GlassTokens.SpecularBorderStart,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Column(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = opacity + 0.06f),
                        Color.White.copy(alpha = opacity)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, Color.White.copy(alpha = 0.03f))
                ),
                shape = shape
            )
            .then(clickableModifier)
            .padding(20.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = GlassTokens.ElectricViolet,
    enabled: Boolean = true,
    testTagStr: String = "glass_button"
) {
    val shape = RoundedCornerShape(16.dp)
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
                    brush = Brush.horizontalGradient(
                        colors = if (enabled) listOf(accentColor, accentColor.copy(alpha = 0.75f))
                        else listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
                    ),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = shape
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
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
                        tint = GlassTokens.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = GlassTokens.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = Icons.Default.Search,
    testTagStr: String = "glass_input"
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .testTag(testTagStr)
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "Search",
                    tint = GlassTokens.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
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
                    tint = GlassTokens.TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
    }
}

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.ElectricViolet
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isSelected) accentColor.copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun GlassRingProgress(
    progress: Float,
    timeText: String,
    statusText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTokens.ElectricViolet
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background glass arc
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Active progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(accentColor, GlassTokens.NeonCyan, accentColor)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                fontSize = 14.sp,
                color = GlassTokens.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

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
    accentColor: Color = GlassTokens.ElectricViolet
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.6f))
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.08f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val pillShape = CircleShape

                Box(
                    modifier = Modifier
                        .testTag("nav_${item.route}")
                        .clip(pillShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.35f)
                            else Color.Transparent
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextMuted
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
    accentColor: Color = GlassTokens.ElectricViolet
) {
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .padding(12.dp)
            .shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.6f))
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.08f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val pillShape = CircleShape

                Box(
                    modifier = Modifier
                        .testTag("nav_rail_${item.route}")
                        .clip(pillShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.35f)
                            else Color.Transparent
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

