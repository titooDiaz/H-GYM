package com.titoodev.h_gym.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.titoodev.h_gym.ui.components.rememberHGymHaptics

@Composable
fun HGymBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onQuickActionClick: () -> Unit
) {
    val haptics = rememberHGymHaptics()
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Inicio
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Home.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio",
                        tint = if (currentRoute == Screen.Home.route) activeColor else inactiveColor
                    )
                }

                // 2. Alimentación
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Nutrition.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Alimentación",
                        tint = if (currentRoute == Screen.Nutrition.route) activeColor else inactiveColor
                    )
                }

                // Central Floating Quick Action Button (+)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
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

                // 3. Entrenamiento (Workout + Subrutinas / Ejercicios)
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Workout.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Entrenamiento",
                        tint = if (currentRoute == Screen.Workout.route || currentRoute == Screen.Routines.route || currentRoute == Screen.Exercises.route) activeColor else inactiveColor
                    )
                }

                // 4. Evolución
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Progress.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Evolución",
                        tint = if (currentRoute == Screen.Progress.route) activeColor else inactiveColor
                    )
                }

                // 5. Comunidad
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onNavigate(Screen.Community.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Comunidad",
                        tint = if (currentRoute == Screen.Community.route) activeColor else inactiveColor
                    )
                }
            }
        }
    }
}
