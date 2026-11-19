package com.titoodev.h_gym.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.titoodev.h_gym.R
import com.titoodev.h_gym.ui.components.HGymDatePickerBar
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import com.titoodev.h_gym.ui.screens.community.CommunityScreen
import com.titoodev.h_gym.ui.screens.exercises.ExerciseCatalogScreen
import com.titoodev.h_gym.ui.screens.home.HomeScreen
import com.titoodev.h_gym.ui.screens.nutrition.NutritionScreen
import com.titoodev.h_gym.ui.screens.progress.ProgressScreen
import com.titoodev.h_gym.ui.screens.routines.RoutinesScreen
import com.titoodev.h_gym.ui.screens.workout.WorkoutScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HGymMainScreen(
    onSignOut: () -> Unit
) {
    val haptics = rememberHGymHaptics()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedDateTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var pendingRoutineIdToStart by remember { mutableStateOf<Long?>(null) }

    var showQuickActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_hgym_logo),
                                contentDescription = "H-GYM Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = Screen.fromRoute(currentRoute).title,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                // Show date picker on Home & Nutrition screens
                if (currentRoute == Screen.Home.route || currentRoute == Screen.Nutrition.route) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        HGymDatePickerBar(
                            selectedTimestamp = selectedDateTimestamp,
                            onDateSelected = { selectedDateTimestamp = it }
                        )
                    }
                }
            }
        },
        bottomBar = {
            HGymBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onQuickActionClick = {
                    showQuickActionSheet = true
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    selectedDateTimestamp = selectedDateTimestamp,
                    onSignOut = onSignOut,
                    onStartWorkout = { routineId ->
                        pendingRoutineIdToStart = routineId
                        navController.navigate(Screen.Workout.route)
                    }
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onStartRoutine = { routineId ->
                        pendingRoutineIdToStart = routineId
                        navController.navigate(Screen.Workout.route)
                    }
                )
            }
            composable(Screen.Exercises.route) { ExerciseCatalogScreen() }
            composable(Screen.Nutrition.route) {
                NutritionScreen(selectedDateTimestamp = selectedDateTimestamp)
            }
            composable(Screen.Workout.route) {
                WorkoutScreen(routineIdToStart = pendingRoutineIdToStart)
            }
            composable(Screen.Progress.route) { ProgressScreen() }
            composable(Screen.Community.route) { CommunityScreen() }
        }
    }

    if (showQuickActionSheet) {
        AlertDialog(
            onDismissRequest = { showQuickActionSheet = false },
            title = { Text("Acción Rápida", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            haptics.performSuccess()
                            showQuickActionSheet = false
                            navController.navigate(Screen.Workout.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("💪 Iniciar Entrenamiento Libre")
                    }

                    Button(
                        onClick = {
                            haptics.performSuccess()
                            showQuickActionSheet = false
                            navController.navigate(Screen.Nutrition.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🥗 Registrar Comida / Calorías")
                    }

                    Button(
                        onClick = {
                            haptics.performSuccess()
                            showQuickActionSheet = false
                            navController.navigate(Screen.Progress.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📈 Registrar Medidas / Peso")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuickActionSheet = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
