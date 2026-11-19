package com.titoodev.h_gym.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Vibrant Duolingo Flat Color Palette
val DuolingoGreen = Color(0xFF58CC02)
val DuolingoGreenDark = Color(0xFF46A302)

val DuolingoBlue = Color(0xFF1CB0F6)
val DuolingoBlueDark = Color(0xFF1899D6)

val DuolingoRed = Color(0xFFFF4B4B)
val DuolingoRedDark = Color(0xFFEA2B2B)

val DuolingoYellow = Color(0xFFFFC800)
val DuolingoYellowDark = Color(0xFFE5B200)

val DuolingoGray = Color(0xFFE5E7EB)
val DuolingoGrayDark = Color(0xFFCE0000)

/**
 * Tactical 3D Rounded Button inspired by Duolingo.
 * Depresses downwards when pressed with haptic feedback.
 */
@Composable
fun DuolingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    shadowColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    icon: (@Composable () -> Unit)? = null
) {
    val haptics = rememberHGymHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowHeight = 5.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isPressed) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 50),
        label = "buttonPressAnimation"
    )

    Box(
        modifier = modifier
            .height(height + shadowHeight)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // 3D Bottom Shadow Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = shadowHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(if (enabled) shadowColor else Color(0xFFD1D5DB))
        )

        // Front Interactive Button Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = animatedOffsetY)
                .clip(RoundedCornerShape(18.dp))
                .background(if (enabled) containerColor else Color(0xFFE5E7EB))
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    haptics.performSuccess()
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    icon()
                }
                Text(
                    text = text.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) contentColor else Color(0xFF9CA3AF),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Rounded Duolingo Card Container with thick border
 */
@Composable
fun DuolingoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

/**
 * Tactile Day Choice Chip (L, M, X, J, V, S, D)
 */
@Composable
fun DuolingoDayChip(
    dayLabel: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHGymHaptics()
    val bgColor = if (isSelected) DuolingoGreen else MaterialTheme.colorScheme.surface
    val shadowColor = if (isSelected) DuolingoGreenDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(shadowColor)
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable {
                haptics.performClick()
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayLabel,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Animated Duolingo Style Progress Bar
 */
@Composable
fun DuolingoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color = DuolingoGreen
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(10.dp))
                .background(barColor)
        )
    }
}
