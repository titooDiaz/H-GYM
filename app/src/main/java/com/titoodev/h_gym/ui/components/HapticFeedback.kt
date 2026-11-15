package com.titoodev.h_gym.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class HGymHaptics(private val haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    fun performClick() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun performSuccess() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberHGymHaptics(): HGymHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { HGymHaptics(haptic) }
}
