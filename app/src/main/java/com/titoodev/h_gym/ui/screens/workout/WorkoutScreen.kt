package com.titoodev.h_gym.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class ExerciseSessionData(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<Pair<Float, Int>> // Pair(weight, reps)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()

    val workoutsWithSetsState by viewModel.workoutsWithSetsState.collectAsState()
    val routinesState by viewModel.routinesWithItemsState.collectAsState()
    val allExercisesState by viewModel.allExercisesState.collectAsState()

    // Active Workout State
    var isWorkoutActive by remember { mutableStateOf(false) }
    var activeWorkoutTitle by remember { mutableStateOf("Entrenamiento Libre") }
    var sessionStartTime by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Rest Timer State
    var restDurationSetting by remember { mutableIntStateOf(90) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var isRestTimerRunning by remember { mutableStateOf(false) }

    // Active Exercises & Sets Builder
    var currentExercises by remember { mutableStateOf(listOf<ExerciseSessionData>()) }

    // Dialogs
    var showSelectExerciseDialog by remember { mutableStateOf(false) }
    var exerciseSearchQuery by remember { mutableStateOf("") }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }
    var newRoutineName by remember { mutableStateOf("") }
    var newRoutineTarget by remember { mutableStateOf("Push") }

    // Session Timer Effect
    LaunchedEffect(isWorkoutActive) {
        if (isWorkoutActive) {
            while (isWorkoutActive) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    // Rest Timer Effect
    LaunchedEffect(isRestTimerRunning) {
        if (isRestTimerRunning && restSecondsRemaining > 0) {
            while (restSecondsRemaining > 0) {
                delay(1000L)
                restSecondsRemaining--
            }
            isRestTimerRunning = false
            haptics.performSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenamientos y Rutinas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Session Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorkoutActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isWorkoutActive) "⚡ Sesión Activa: $activeWorkoutTitle" else "Iniciar Entrenamiento",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (isWorkoutActive) {
                                val minutes = elapsedSeconds / 60
                                val seconds = elapsedSeconds % 60
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isWorkoutActive) "Selecciona ejercicios del catálogo real de Room y registra cada serie." else "Comienza un entrenamiento libre o selecciona una rutina para registrar hora, fecha y series exactas.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        if (isWorkoutActive) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Total Volume Live Calculation
                            val totalSessionVolume = currentExercises.sumOf { ex ->
                                ex.sets.sumOf { (weight, reps) -> (weight * reps).toDouble() }
                            }
                            Text(
                                text = "Volumen Total: ${totalSessionVolume.toInt()} kg",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ejercicios (${currentExercises.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = {
                                        haptics.performClick()
                                        showSelectExerciseDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Añadir Ejercicio")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            currentExercises.forEach { exData ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = exData.exerciseName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                            IconButton(onClick = {
                                                haptics.performClick()
                                                currentExercises = currentExercises.filter { it.exerciseId != exData.exerciseId }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }

                                        if (exData.sets.isEmpty()) {
                                            Text("Sin series registradas aún.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        } else {
                                            exData.sets.forEachIndexed { sIdx, setItem ->
                                                Text("• Serie ${sIdx + 1}: ${setItem.first} kg × ${setItem.second} reps (${(setItem.first * setItem.second).toInt()} kg)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        var setWeight by remember { mutableStateOf("60") }
                                        var setReps by remember { mutableStateOf("10") }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = setWeight,
                                                onValueChange = { setWeight = it },
                                                label = { Text("Kg") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            OutlinedTextField(
                                                value = setReps,
                                                onValueChange = { setReps = it },
                                                label = { Text("Reps") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Button(
                                                onClick = {
                                                    val w = setWeight.toFloatOrNull() ?: 0f
                                                    val r = setReps.toIntOrNull() ?: 0
                                                    if (w > 0 && r > 0) {
                                                        haptics.performSuccess()
                                                        currentExercises = currentExercises.map {
                                                            if (it.exerciseId == exData.exerciseId) {
                                                                it.copy(sets = it.sets + Pair(w, r))
                                                            } else it
                                                        }
                                                        restSecondsRemaining = restDurationSetting
                                                        isRestTimerRunning = true
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("+ Serie")
                                            }
                                        }
                                    }
                                }
                            }

                            // Rest Timer Config & Bar
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Descanso:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(30, 60, 90, 120).forEach { sec ->
                                        FilterChip(
                                            selected = restDurationSetting == sec,
                                            onClick = {
                                                haptics.performClick()
                                                restDurationSetting = sec
                                            },
                                            label = { Text("${sec}s", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            if (isRestTimerRunning) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                            Text("Temporizador de descanso", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                        Text("${restSecondsRemaining}s", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                haptics.performSuccess()
                                if (!isWorkoutActive) {
                                    activeWorkoutTitle = "Entrenamiento Libre"
                                    isWorkoutActive = true
                                    sessionStartTime = System.currentTimeMillis()
                                    elapsedSeconds = 0L
                                    val firstEx = allExercisesState.firstOrNull()
                                    currentExercises = if (firstEx != null) {
                                        listOf(ExerciseSessionData(firstEx.id, firstEx.name, emptyList()))
                                    } else emptyList()
                                } else {
                                    val setsToSave = mutableListOf<WorkoutSetEntity>()
                                    var setCounter = 1
                                    currentExercises.forEach { exData ->
                                        exData.sets.forEach { setPair ->
                                            setsToSave.add(
                                                WorkoutSetEntity(
                                                    workoutId = 0L,
                                                    exerciseId = exData.exerciseId,
                                                    setNumber = setCounter++,
                                                    weight = setPair.first,
                                                    reps = setPair.second,
                                                    rirOrRpe = null
                                                )
                                            )
                                        }
                                    }

                                    val totalVol = currentExercises.sumOf { ex ->
                                        ex.sets.sumOf { (w, r) -> (w * r).toDouble() }
                                    }.toInt()

                                    viewModel.saveWorkoutSession(
                                        title = activeWorkoutTitle,
                                        startTime = sessionStartTime,
                                        durationSeconds = elapsedSeconds,
                                        notes = "Volumen: $totalVol kg • ${setsToSave.size} series en total",
                                        sets = setsToSave
                                    )

                                    isWorkoutActive = false
                                    isRestTimerRunning = false
                                    currentExercises = emptyList()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWorkoutActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(if (isWorkoutActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isWorkoutActive) "Finalizar y Guardar en Room" else "Nuevo Entrenamiento Libre", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Routines Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rutinas Personalizadas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(onClick = {
                        haptics.performClick()
                        showCreateRoutineDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear Rutina", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            val defaultRoutines = listOf(
                Pair("Push (Pecho, Hombro, Tríceps)", "Enfocado en empuje"),
                Pair("Pull (Espalda, Bíceps)", "Enfocado en tracción"),
                Pair("Legs (Pierna Completa)", "Enfocado en tren inferior")
            )

            items(defaultRoutines) { routine ->
                Card(
                    onClick = {
                        haptics.performSuccess()
                        activeWorkoutTitle = routine.first
                        isWorkoutActive = true
                        sessionStartTime = System.currentTimeMillis()
                        elapsedSeconds = 0L
                        val matchingExs = allExercisesState.take(3)
                        currentExercises = matchingExs.map { ExerciseSessionData(it.id, it.name, emptyList()) }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = routine.first, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = routine.second, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = {
                                haptics.performSuccess()
                                activeWorkoutTitle = routine.first
                                isWorkoutActive = true
                                sessionStartTime = System.currentTimeMillis()
                                elapsedSeconds = 0L
                                val matchingExs = allExercisesState.take(3)
                                currentExercises = matchingExs.map { ExerciseSessionData(it.id, it.name, emptyList()) }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Iniciar")
                        }
                    }
                }
            }

            items(routinesState) { customRoutine ->
                Card(
                    onClick = {
                        haptics.performSuccess()
                        activeWorkoutTitle = customRoutine.routine.name
                        isWorkoutActive = true
                        sessionStartTime = System.currentTimeMillis()
                        elapsedSeconds = 0L
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = customRoutine.routine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "${customRoutine.routine.targetType} • ${customRoutine.routine.description}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Completed Workouts History Header
            item {
                Text(
                    text = "Historial de Sesiones en Room",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (workoutsWithSetsState.isEmpty()) {
                item {
                    Text(
                        text = "Aún no has registrado sesiones de entrenamiento.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(workoutsWithSetsState) { workoutWithSets ->
                    val workout = workoutWithSets.workout
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = workout.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                Text(text = sdf.format(Date(workout.date)), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val mins = workout.durationSeconds / 60
                            val setNum = workoutWithSets.sets.size
                            Text(text = "Duración: $mins min • $setNum series • ${workout.notes ?: ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }

    // Select Exercise Dialog from Catálogo Real de Room
    if (showSelectExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showSelectExerciseDialog = false },
            title = { Text("Seleccionar Ejercicio del Catálogo") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    OutlinedTextField(
                        value = exerciseSearchQuery,
                        onValueChange = { exerciseSearchQuery = it },
                        label = { Text("Buscar por nombre o músculo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val filteredExercises = allExercisesState.filter {
                        it.name.contains(exerciseSearchQuery, ignoreCase = true) ||
                        it.muscleGroup.contains(exerciseSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredExercises) { exItem ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.performSuccess()
                                        if (currentExercises.none { it.exerciseId == exItem.id }) {
                                            currentExercises = currentExercises + ExerciseSessionData(exItem.id, exItem.name, emptyList())
                                        }
                                        showSelectExerciseDialog = false
                                        exerciseSearchQuery = ""
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = exItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "${exItem.muscleGroup} • ${exItem.equipment ?: "Libre"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectExerciseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showCreateRoutineDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRoutineDialog = false },
            title = { Text("Crear Rutina Personalizada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRoutineName,
                        onValueChange = { newRoutineName = it },
                        label = { Text("Nombre de la rutina") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoutineTarget,
                        onValueChange = { newRoutineTarget = it },
                        label = { Text("Tipo (Push, Pull, Legs, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoutineName.isNotBlank()) {
                        haptics.performSuccess()
                        viewModel.saveRoutine(
                            name = newRoutineName,
                            description = "Rutina personalizada de ${newRoutineTarget}",
                            targetType = newRoutineTarget
                        )
                        newRoutineName = ""
                        showCreateRoutineDialog = false
                    }
                }) {
                    Text("Guardar Rutina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoutineDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
