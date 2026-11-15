package com.titoodev.h_gym.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Nutrition : Screen("nutrition", "Alimentación")
    object Workout : Screen("workout", "Entrenamiento")
    object Progress : Screen("progress", "Evolución")
    object Community : Screen("community", "Comunidad")
}
