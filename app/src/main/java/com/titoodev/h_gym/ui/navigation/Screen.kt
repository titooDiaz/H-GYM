package com.titoodev.h_gym.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Hoy")
    object Routines : Screen("routines", "Mis Rutinas")
    object Exercises : Screen("exercises", "Biblioteca de Ejercicios")
    object Workout : Screen("workout", "Entrenamiento")
    object Progress : Screen("progress", "Mapa de Constancia y Evolución")
    object Nutrition : Screen("nutrition", "Alimentación")
    object Community : Screen("community", "Comunidad")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Home.route -> Home
                Routines.route -> Routines
                Exercises.route -> Exercises
                Workout.route -> Workout
                Progress.route -> Progress
                Nutrition.route -> Nutrition
                Community.route -> Community
                else -> Home
            }
        }
    }
}
