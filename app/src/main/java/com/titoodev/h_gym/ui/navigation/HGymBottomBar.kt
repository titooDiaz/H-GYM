package com.titoodev.h_gym.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.titoodev.h_gym.ui.components.DuolingoGreen
import com.titoodev.h_gym.ui.components.rememberHGymHaptics

@Composable
fun HGymBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onQuickActionClick: () -> Unit
) {
    val haptics = rememberHGymHaptics()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Hoy (Home)
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Home.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Hoy",
                        tint = if (currentRoute == Screen.Home.route) DuolingoGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // 2. Rutinas
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Routines.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Rutinas",
                        tint = if (currentRoute == Screen.Routines.route) DuolingoGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Central Floating Quick Action Button (+)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DuolingoGreen)
                        .clickable {
                            haptics.performSuccess()
                            onQuickActionClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Acción Rápida",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // 3. Ejercicios
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Exercises.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Ejercicios",
                        tint = if (currentRoute == Screen.Exercises.route) DuolingoGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // 4. Progreso / Mapa de Cumplimiento
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Progress.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Progreso",
                        tint = if (currentRoute == Screen.Progress.route) DuolingoGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
