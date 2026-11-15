package com.titoodev.h_gym.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen() {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { HGymDatabase.getDatabase(context) }

    val exercisesState by db.exerciseDao().getAllExercises().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseName by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("Pecho") }
    var equipment by remember { mutableStateOf("Barra") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Ejercicios", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.performSuccess()
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Ejercicio")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Ejercicios Registrados en Room",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (exercisesState.isEmpty()) {
                item {
                    Text(
                        text = "No hay ejercicios guardados. Pulsa + para añadir uno.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(exercisesState) { exercise ->
                    Card(
                        onClick = { haptics.performSuccess() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = exercise.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${exercise.muscleGroup} • ${exercise.equipment ?: "Libre"}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Añadir Nuevo Ejercicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Nombre del ejercicio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = muscleGroup,
                        onValueChange = { muscleGroup = it },
                        label = { Text("Grupo muscular (Pecho, Espalda, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("Equipamiento (Barra, Mancuerna, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (exerciseName.isNotBlank()) {
                        coroutineScope.launch {
                            db.exerciseDao().insertExercise(
                                ExerciseEntity(
                                    name = exerciseName,
                                    muscleGroup = muscleGroup,
                                    secondaryMuscles = null,
                                    description = null,
                                    equipment = equipment,
                                    exerciseType = "Fuerza",
                                    personalNotes = null,
                                    isCustom = true
                                )
                            )
                            exerciseName = ""
                            showAddDialog = false
                            haptics.performSuccess()
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
