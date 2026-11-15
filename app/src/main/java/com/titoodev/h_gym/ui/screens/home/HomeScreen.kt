package com.titoodev.h_gym.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.titoodev.h_gym.data.local.preferences.ThemePreferences
import com.titoodev.h_gym.data.local.preferences.ThemeSettings
import com.titoodev.h_gym.ui.components.ThemeSelectorCard
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import com.titoodev.h_gym.ui.theme.ThemeVariant
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    selectedDateTimestamp: Long = System.currentTimeMillis(),
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themePreferences = remember { ThemePreferences(context) }
    val themeSettings by themePreferences.themeSettingsFlow.collectAsState(
        initial = ThemeSettings(ThemeVariant.PINK, true)
    )

    LaunchedEffect(selectedDateTimestamp) {
        viewModel.updateSelectedDate(selectedDateTimestamp)
    }

    val userState by viewModel.userState.collectAsState()
    val latestMeasurementState by viewModel.latestMeasurementState.collectAsState()
    val foodLogsState by viewModel.foodLogsForSelectedDate.collectAsState()
    val workoutsState by viewModel.workoutsForSelectedDate.collectAsState()

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userName = userState?.name ?: firebaseUser?.displayName ?: "Atleta H-GYM"
    val userEmail = userState?.email ?: firebaseUser?.email ?: "Modo offline / Invitado"
    val currentWeight = latestMeasurementState?.weight ?: userState?.currentWeight ?: 70.0f
    val userGoals = userState?.goals?.ifBlank { "Ganar masa muscular" } ?: "Ganar masa muscular"

    var showThemeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptics.performClick()
                            showThemeDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cambiar Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.performSuccess()
                            FirebaseAuth.getInstance().signOut()
                            onSignOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Overview Cards: Weight & Goals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(text = "Peso Actual", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$currentWeight kg", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(text = "Objetivo", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = userGoals, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    }
                }
            }
        }

        // Daily Summary Card for Selected Date
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Resumen del Día Seleccionado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    val dailyCalories = foodLogsState.sumOf { it.calories }
                    val workoutsCount = workoutsState.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Calorías Ingeridas", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$dailyCalories kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Column {
                            Text(text = "Sesiones Entrenadas", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$workoutsCount sesión(es)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Weekly Activity Bar Chart Card (Real Data)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Actividad de la Semana", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    val weekDays = listOf("L", "M", "X", "J", "V", "S", "D")
                    val currentWeekFactors = remember(selectedDateTimestamp, foodLogsState, workoutsState) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedDateTimestamp
                            firstDayOfWeek = Calendar.MONDAY
                            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        }
                        weekDays.map { dayLabel ->
                            val dayStart = com.titoodev.h_gym.util.DateUtils.getStartOfDay(cal.timeInMillis)
                            val dayEnd = com.titoodev.h_gym.util.DateUtils.getEndOfDay(cal.timeInMillis)
                            cal.add(Calendar.DAY_OF_YEAR, 1)

                            val hasWorkout = workoutsState.any { it.workout.date in dayStart..dayEnd }
                            val hasFood = foodLogsState.any { it.timestamp in dayStart..dayEnd }
                            val factor = when {
                                hasWorkout && hasFood -> 1.0f
                                hasWorkout -> 0.8f
                                hasFood -> 0.5f
                                else -> 0.2f
                            }
                            Pair(dayLabel, factor)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        currentWeekFactors.forEach { (dayLabel, heightFactor) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((75 * heightFactor).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (heightFactor > 0.4f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
                                )
                                Text(text = dayLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Personalizar Paleta de Tema") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeSelectorCard(
                        currentVariant = themeSettings.variant,
                        isDarkMode = themeSettings.isDarkMode,
                        onVariantSelected = { variant ->
                            coroutineScope.launch {
                                themePreferences.setThemeVariant(variant)
                                haptics.performSuccess()
                            }
                        },
                        onDarkModeToggled = { darkMode ->
                            coroutineScope.launch {
                                themePreferences.setDarkMode(darkMode)
                                haptics.performSuccess()
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
