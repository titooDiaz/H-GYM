package com.titoodev.h_gym.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import com.titoodev.h_gym.ui.components.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class ActiveSetItem(
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val unit: String = "kg",
    val durationSeconds: Long = 0L,
    val notes: String? = null
)

data class ActiveExerciseItem(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<ActiveSetItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    routineIdToStart: Long? = null,
    viewModel: WorkoutViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()

    val workoutsWithSetsState by viewModel.workoutsWithSetsState.collectAsState()
    val routinesState by viewModel.routinesWithItemsState.collectAsState()
    val allExercisesState by viewModel.allExercisesState.collectAsState()

    // Active Workout State
    var isWorkoutActive by remember { mutableStateOf(false) }
    var activeWorkoutTitle by remember { mutableStateOf("Entrenamiento Libre") }
    var activeRoutineId by remember { mutableStateOf<Long?>(null) }
    var sessionStartTime by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Selected Unit Setting (kg or lb)
    var selectedUnit by remember { mutableStateOf("kg") }

    // Active Exercises Builder
    var currentExercises by remember { mutableStateOf(listOf<ActiveExerciseItem>()) }

    // Rest & Set Timers State
    var restDurationSetting by remember { mutableIntStateOf(90) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var isRestTimerRunning by remember { mutableStateOf(false) }

    var isSetTimerRunning by remember { mutableStateOf(false) }
    var setTimerSeconds by remember { mutableLongStateOf(0L) }

    // Celebration Summary Dialog State
    var showCelebrationSummary by remember { mutableStateOf(false) }
    var completedTotalVolume by remember { mutableIntStateOf(0) }
    var completedTotalSets by remember { mutableIntStateOf(0) }
    var completedDuration by remember { mutableLongStateOf(0L) }

    // Select Exercise Dialog
    var showSelectExerciseDialog by remember { mutableStateOf(false) }
    var exerciseSearchQuery by remember { mutableStateOf("") }

    // Auto-start from Routine if passed
    LaunchedEffect(routineIdToStart) {
        if (routineIdToStart != null && !isWorkoutActive) {
            val rItem = routinesState.find { it.routine.id == routineIdToStart }
            if (rItem != null) {
                activeWorkoutTitle = rItem.routine.name
                activeRoutineId = rItem.routine.id
                isWorkoutActive = true
                sessionStartTime = System.currentTimeMillis()
                elapsedSeconds = 0L

                val exerciseMap = allExercisesState.associateBy { it.id }
                currentExercises = rItem.items.sortedBy { it.orderIndex }.mapNotNull { item ->
                    val ex = exerciseMap[item.exerciseId]
                    if (ex != null) ActiveExerciseItem(ex.id, ex.name, emptyList()) else null
                }
            }
        }
    }

    // Session Timer Effect
    LaunchedEffect(isWorkoutActive) {
        if (isWorkoutActive) {
            while (isWorkoutActive) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    // Set Timer Effect
    LaunchedEffect(isSetTimerRunning) {
        if (isSetTimerRunning) {
            while (isSetTimerRunning) {
                delay(1000L)
                setTimerSeconds++
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Active Session Card (Duolingo Style)
            item {
                DuolingoCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isWorkoutActive) "⚡ Sesión Activa" else "Iniciar Nuevo Entrenamiento",
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
                                    fontWeight = FontWeight.Black,
                                    color = DuolingoGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isWorkoutActive) {
                            // Live Progress Bar (Exercise X of Y)
                            val totalExCount = currentExercises.size.coerceAtLeast(1)
                            val completedExCount = currentExercises.count { it.sets.isNotEmpty() }
                            val progressFactor = completedExCount.toFloat() / totalExCount.toFloat()

                            Text(
                                text = "Progreso del entrenamiento: Ejercicio $completedExCount de $totalExCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            DuolingoProgressBar(progress = progressFactor)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Unit Selector (kg / lb)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Unidad de Peso:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("kg", "lb").forEach { u ->
                                        FilterChip(
                                            selected = selectedUnit == u,
                                            onClick = {
                                                haptics.performClick()
                                                selectedUnit = u
                                            },
                                            label = { Text(u.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Exercises Builder List
                            currentExercises.forEachIndexed { exIndex, exItem ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header with Up / Down Reorder Arrows
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${exIndex + 1}. ${exItem.exerciseName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (exIndex > 0) {
                                                    IconButton(onClick = {
                                                        haptics.performClick()
                                                        val list = currentExercises.toMutableList()
                                                        val tmp = list[exIndex]
                                                        list[exIndex] = list[exIndex - 1]
                                                        list[exIndex - 1] = tmp
                                                        currentExercises = list
                                                    }) {
                                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Subir", modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                if (exIndex < currentExercises.size - 1) {
                                                    IconButton(onClick = {
                                                        haptics.performClick()
                                                        val list = currentExercises.toMutableList()
                                                        val tmp = list[exIndex]
                                                        list[exIndex] = list[exIndex + 1]
                                                        list[exIndex + 1] = tmp
                                                        currentExercises = list
                                                    }) {
                                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar", modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                IconButton(onClick = {
                                                    haptics.performClick()
                                                    currentExercises = currentExercises.filter { it.exerciseId != exItem.exerciseId }
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = DuolingoRed, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }

                                        // Target Reference (Last recorded set to beat!)
                                        val prevSets = workoutsWithSetsState.flatMap { it.sets }.filter { it.exerciseId == exItem.exerciseId }
                                        val bestPrev = prevSets.maxByOrNull { it.weight }
                                        if (bestPrev != null) {
                                            Text(
                                                text = "🎯 Marca a superar: ${bestPrev.weight} ${bestPrev.unit} × ${bestPrev.reps} reps",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DuolingoGreen
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }

                                        // Registered Sets List
                                        if (exItem.sets.isEmpty()) {
                                            Text("Sin series registradas aún.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        } else {
                                            exItem.sets.forEachIndexed { sIdx, setItem ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "• Serie ${sIdx + 1}: ${setItem.weight} ${setItem.unit} × ${setItem.reps} reps (${setItem.durationSeconds}s)${if (!setItem.notes.isNullOrBlank()) " [${setItem.notes}]" else ""}",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    IconButton(onClick = {
                                                        haptics.performClick()
                                                        val updatedSets = exItem.sets.filterIndexed { i, _ -> i != sIdx }
                                                        currentExercises = currentExercises.map {
                                                            if (it.exerciseId == exItem.exerciseId) it.copy(sets = updatedSets) else it
                                                        }
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Borrar serie", tint = DuolingoRed, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Set Builder Row
                                        var setWeight by remember { mutableStateOf("60") }
                                        var setReps by remember { mutableStateOf("10") }
                                        var setNotes by remember { mutableStateOf("") }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = setWeight,
                                                onValueChange = { setWeight = it },
                                                label = { Text(selectedUnit) },
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
                                            OutlinedTextField(
                                                value = setNotes,
                                                onValueChange = { setNotes = it },
                                                label = { Text("Nota") },
                                                modifier = Modifier.weight(1.2f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Interactive Set Timer & Add Set Button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    haptics.performClick()
                                                    if (!isSetTimerRunning) {
                                                        isSetTimerRunning = true
                                                        setTimerSeconds = 0L
                                                    } else {
                                                        isSetTimerRunning = false
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(if (isSetTimerRunning) Icons.Default.Stop else Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isSetTimerRunning) "Detener (${setTimerSeconds}s)" else "Timer Serie")
                                            }

                                            Button(
                                                onClick = {
                                                    val w = setWeight.toFloatOrNull() ?: 0f
                                                    val r = setReps.toIntOrNull() ?: 0
                                                    if (w > 0 && r > 0) {
                                                        haptics.performSuccess()
                                                        val newSet = ActiveSetItem(
                                                            setNumber = exItem.sets.size + 1,
                                                            weight = w,
                                                            reps = r,
                                                            unit = selectedUnit,
                                                            durationSeconds = setTimerSeconds,
                                                            notes = setNotes.ifBlank { null }
                                                        )
                                                        currentExercises = currentExercises.map {
                                                            if (it.exerciseId == exItem.exerciseId) {
                                                                it.copy(sets = it.sets + newSet)
                                                            } else it
                                                        }
                                                        isSetTimerRunning = false
                                                        setTimerSeconds = 0L
                                                        setNotes = ""
                                                        restSecondsRemaining = restDurationSetting
                                                        isRestTimerRunning = true
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DuolingoGreen)
                                            ) {
                                                Text("+ Guardar Serie", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            DuolingoButton(
                                text = "Añadir Otro Ejercicio",
                                onClick = { showSelectExerciseDialog = true },
                                containerColor = DuolingoBlue,
                                shadowColor = DuolingoBlueDark,
                                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DuolingoButton(
                                text = "Finalizar y Celebrar",
                                onClick = {
                                    val setsToSave = mutableListOf<WorkoutSetEntity>()
                                    var setCounter = 1
                                    currentExercises.forEach { exData ->
                                        exData.sets.forEach { setItem ->
                                            setsToSave.add(
                                                WorkoutSetEntity(
                                                    workoutId = 0L,
                                                    exerciseId = exData.exerciseId,
                                                    setNumber = setCounter++,
                                                    weight = setItem.weight,
                                                    reps = setItem.reps,
                                                    durationSeconds = setItem.durationSeconds,
                                                    unit = setItem.unit,
                                                    notes = setItem.notes
                                                )
                                            )
                                        }
                                    }

                                    val totalVol = currentExercises.sumOf { ex ->
                                        ex.sets.sumOf { (w, r) -> (w * r).toDouble() }
                                    }.toInt()

                                    completedTotalVolume = totalVol
                                    completedTotalSets = setsToSave.size
                                    completedDuration = elapsedSeconds

                                    viewModel.saveWorkoutSession(
                                        title = activeWorkoutTitle,
                                        startTime = sessionStartTime,
                                        durationSeconds = elapsedSeconds,
                                        notes = "Volumen: $totalVol $selectedUnit • ${setsToSave.size} series completadas",
                                        sets = setsToSave
                                    )

                                    // Save new order to Routine if routine active
                                    if (activeRoutineId != null && currentExercises.isNotEmpty()) {
                                        viewModel.saveFullRoutine(
                                            id = activeRoutineId!!,
                                            name = activeWorkoutTitle,
                                            daysOfWeek = "L,M,X,J,V",
                                            exerciseIds = currentExercises.map { it.exerciseId }
                                        )
                                    }

                                    isWorkoutActive = false
                                    isRestTimerRunning = false
                                    isSetTimerRunning = false
                                    showCelebrationSummary = true
                                    currentExercises = emptyList()
                                },
                                containerColor = DuolingoGreen,
                                shadowColor = DuolingoGreenDark,
                                icon = { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White) }
                            )
                        } else {
                            DuolingoButton(
                                text = "Nuevo Entrenamiento Libre",
                                onClick = {
                                    activeWorkoutTitle = "Entrenamiento Libre"
                                    activeRoutineId = null
                                    isWorkoutActive = true
                                    sessionStartTime = System.currentTimeMillis()
                                    elapsedSeconds = 0L
                                    val firstEx = allExercisesState.firstOrNull()
                                    currentExercises = if (firstEx != null) {
                                        listOf(ActiveExerciseItem(firstEx.id, firstEx.name, emptyList()))
                                    } else emptyList()
                                },
                                containerColor = DuolingoGreen,
                                shadowColor = DuolingoGreenDark,
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White) }
                            )
                        }
                    }
                }
            }

            // Completed Workouts History Header
            item {
                Text(
                    text = "Historial de Sesiones Completadas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (workoutsWithSetsState.isEmpty()) {
                item {
                    Text(
                        text = "Aún no has completado sesiones de entrenamiento.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(workoutsWithSetsState) { workoutWithSets ->
                    val workout = workoutWithSets.workout
                    DuolingoCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = workout.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                Text(text = sdf.format(Date(workout.date)), fontSize = 12.sp, color = DuolingoGreen, fontWeight = FontWeight.Bold)
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

    // Celebration Summary Dialog (Duolingo Style)
    if (showCelebrationSummary) {
        AlertDialog(
            onDismissRequest = { showCelebrationSummary = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = DuolingoYellow, modifier = Modifier.size(32.dp))
                    Text("¡Entrenamiento Completado!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎉 ¡Felicitaciones! Has mantenido tu racha de constancia activa.", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val mins = completedDuration / 60
                            val secs = completedDuration % 60
                            Text("⏱️ Tiempo Total: ${mins}m ${secs}s", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("🏋️ Series Completadas: $completedTotalSets series", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("🔥 Volumen Total Movido: $completedTotalVolume $selectedUnit", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DuolingoGreen)
                        }
                    }
                }
            },
            confirmButton = {
                DuolingoButton(
                    text = "¡Genial!",
                    onClick = { showCelebrationSummary = false },
                    modifier = Modifier.width(130.dp)
                )
            }
        )
    }

    // Select Exercise Dialog
    if (showSelectExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showSelectExerciseDialog = false },
            title = { Text("Añadir Ejercicio", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    OutlinedTextField(
                        value = exerciseSearchQuery,
                        onValueChange = { exerciseSearchQuery = it },
                        label = { Text("Buscar ejercicio") },
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
                                            currentExercises = currentExercises + ActiveExerciseItem(exItem.id, exItem.name, emptyList())
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
}
