package com.titoodev.h_gym.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import com.titoodev.h_gym.ui.components.*
import com.titoodev.h_gym.util.DateUtils
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ActiveSetItem(
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val unit: String = "kg",
    val durationSeconds: Long = 0L,
    val notes: String? = null,
    val isGhost: Boolean = false // Pre-filled set from last workout (dimmed until confirmed)
)

data class ActiveExerciseItem(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String = "General",
    val localPhotoPath: String? = null,
    val sets: List<ActiveSetItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    routineIdToStart: Long? = null,
    viewModel: WorkoutViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()
    var workoutSubTab by remember { mutableIntStateOf(0) }

    val workoutsWithSetsState by viewModel.workoutsWithSetsState.collectAsState()
    val routinesState by viewModel.routinesWithItemsState.collectAsState()
    val allExercisesState by viewModel.allExercisesState.collectAsState()

    // Active Workout State
    var isWorkoutActive by remember { mutableStateOf(false) }
    var activeWorkoutTitle by remember { mutableStateOf("Entrenamiento Libre") }
    var activeRoutineId by remember { mutableStateOf<Long?>(null) }
    var sessionStartTime by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Unit Preference (kg / lb)
    var selectedUnit by remember { mutableStateOf("kg") }

    // Active Exercises
    var currentExercises by remember { mutableStateOf(listOf<ActiveExerciseItem>()) }

    // Rest & Set Timers State
    var restDurationSetting by remember { mutableIntStateOf(90) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var isRestTimerRunning by remember { mutableStateOf(false) }

    var isSetTimerRunning by remember { mutableStateOf(false) }
    var setTimerSeconds by remember { mutableLongStateOf(0L) }

    // Celebration & Records State
    var showCelebrationSummary by remember { mutableStateOf(false) }
    var completedTotalVolume by remember { mutableIntStateOf(0) }
    var completedTotalSets by remember { mutableIntStateOf(0) }
    var completedDuration by remember { mutableLongStateOf(0L) }
    var newRecordsBrokenCount by remember { mutableIntStateOf(0) }

    // Select Exercise Modal State
    var showSelectExerciseDialog by remember { mutableStateOf(false) }
    var exerciseSearchQuery by remember { mutableStateOf("") }
    var selectedFilterMuscleGroup by remember { mutableStateOf("Todos") }

    // Today's scheduled routine
    val todayCode = remember { DateUtils.getCurrentDayOfWeekCode() }
    val todayRoutineItem = remember(routinesState, todayCode) {
        routinesState.find { r ->
            r.routine.daysOfWeek.split(",").map { it.trim() }.contains(todayCode)
        }
    }

    // Function to load exercises with ghost sets from history
    fun loadExerciseWithHistory(exId: Long, exName: String, muscle: String, photo: String?, routineId: Long?): ActiveExerciseItem {
        val exerciseMap = allExercisesState.associateBy { it.id }
        val ex = exerciseMap[exId]

        // Find previous workout sets for this routine or general exercise history
        val prevRoutineWorkout = workoutsWithSetsState
            .filter { (routineId != null && it.workout.notes?.contains("Routine:$routineId") == true) || it.sets.any { s -> s.exerciseId == exId } }
            .maxByOrNull { it.workout.date }

        val prevSets = prevRoutineWorkout?.sets?.filter { it.exerciseId == exId } ?: emptyList()

        val ghostSets = prevSets.mapIndexed { idx, s ->
            ActiveSetItem(
                setNumber = idx + 1,
                weight = s.weight,
                reps = s.reps,
                unit = s.unit,
                durationSeconds = s.durationSeconds,
                notes = s.notes,
                isGhost = true
            )
        }

        return ActiveExerciseItem(
            exerciseId = exId,
            exerciseName = exName,
            muscleGroup = ex?.muscleGroup ?: muscle,
            localPhotoPath = ex?.localPhotoPath ?: photo,
            sets = ghostSets
        )
    }

    // Auto-start from Routine if passed
    LaunchedEffect(routineIdToStart, routinesState, allExercisesState) {
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
                    if (ex != null) {
                        loadExerciseWithHistory(ex.id, ex.name, ex.muscleGroup, ex.localPhotoPath, rItem.routine.id)
                    } else null
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isWorkoutActive) {
            SecondaryTabRow(selectedTabIndex = workoutSubTab) {
                Tab(
                    selected = workoutSubTab == 0,
                    onClick = {
                        haptics.performClick()
                        workoutSubTab = 0
                    },
                    text = { Text("Entrenar", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = workoutSubTab == 1,
                    onClick = {
                        haptics.performClick()
                        workoutSubTab = 1
                    },
                    text = { Text("Mis Rutinas", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = workoutSubTab == 2,
                    onClick = {
                        haptics.performClick()
                        workoutSubTab = 2
                    },
                    text = { Text("Biblioteca", fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                !isWorkoutActive && workoutSubTab == 1 -> {
                    com.titoodev.h_gym.ui.screens.routines.RoutinesScreen(
                        onStartRoutine = { routineId ->
                            workoutSubTab = 0
                            val rItem = routinesState.find { it.routine.id == routineId }
                            if (rItem != null) {
                                activeWorkoutTitle = rItem.routine.name
                                activeRoutineId = rItem.routine.id
                                isWorkoutActive = true
                                sessionStartTime = System.currentTimeMillis()
                                elapsedSeconds = 0L

                                val exerciseMap = allExercisesState.associateBy { it.id }
                                currentExercises = rItem.items.sortedBy { it.orderIndex }.mapNotNull { item ->
                                    val ex = exerciseMap[item.exerciseId]
                                    if (ex != null) loadExerciseWithHistory(ex.id, ex.name, ex.muscleGroup, ex.localPhotoPath, rItem.routine.id) else null
                                }
                            }
                        }
                    )
                }
                !isWorkoutActive && workoutSubTab == 2 -> {
                    com.titoodev.h_gym.ui.screens.exercises.ExerciseCatalogScreen()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
        // Sticky Session Control Top Card
        if (isWorkoutActive) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "⚡ $activeWorkoutTitle",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val mins = elapsedSeconds / 60
                                val secs = elapsedSeconds % 60
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d minutos", mins, secs),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Unit Switcher Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Progress Bar
                        val totalExCount = currentExercises.size.coerceAtLeast(1)
                        val completedExCount = currentExercises.count { it.sets.any { s -> !s.isGhost } }
                        val progressFactor = completedExCount.toFloat() / totalExCount.toFloat()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progreso: Ejercicio $completedExCount de $totalExCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text("${(progressFactor * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        DuolingoProgressBar(progress = progressFactor)

                        // Floating Rest Timer Bar (if active)
                        if (isRestTimerRunning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DuolingoGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DuolingoGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = DuolingoGreen)
                                        Text(
                                            text = "Descanso: ${restSecondsRemaining}s",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = DuolingoGreen
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                haptics.performClick()
                                                restSecondsRemaining += 30
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("+30s", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                haptics.performSuccess()
                                                isRestTimerRunning = false
                                                restSecondsRemaining = 0
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DuolingoGreen)
                                        ) {
                                            Text("Saltar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Exercise Cards Board
            items(currentExercises.indices.toList()) { exIndex ->
                val exItem = currentExercises[exIndex]

                DuolingoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Header with Photo + Name + Reorder Arrows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (exItem.localPhotoPath != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(exItem.localPhotoPath)),
                                        contentDescription = exItem.exerciseName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Column {
                                    Text(
                                        text = "${exIndex + 1}. ${exItem.exerciseName}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = exItem.muscleGroup,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
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

                        // Personal Best Reference Target
                        val allPastSets = workoutsWithSetsState.flatMap { it.sets }.filter { it.exerciseId == exItem.exerciseId }
                        val bestPastSet = allPastSets.maxByOrNull { it.weight }
                        if (bestPastSet != null) {
                            Text(
                                text = "🎯 Récord a superar: ${bestPastSet.weight} ${bestPastSet.unit} × ${bestPastSet.reps} reps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuolingoGreen
                            )
                        } else {
                            Text(
                                text = "✨ Aún sin registros. ¡Asegura tu primera marca hoy!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Registered Sets Table
                        if (exItem.sets.isEmpty()) {
                            Text("Aún no has agregado series.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        } else {
                            exItem.sets.forEachIndexed { sIdx, setItem ->
                                if (setItem.isGhost) {
                                    // Ghost pre-filled set card (dimmed)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• Serie ${sIdx + 1} (Anterior): ${setItem.weight} ${setItem.unit} × ${setItem.reps} reps",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Button(
                                                onClick = {
                                                    haptics.performSuccess()
                                                    val updatedSets = exItem.sets.mapIndexed { i, s ->
                                                        if (i == sIdx) s.copy(isGhost = false) else s
                                                    }
                                                    currentExercises = currentExercises.map {
                                                        if (it.exerciseId == exItem.exerciseId) it.copy(sets = updatedSets) else it
                                                    }
                                                    restSecondsRemaining = restDurationSetting
                                                    isRestTimerRunning = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DuolingoGreen)
                                            ) {
                                                Text("Confirmar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    // Active Confirmed Set Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isNewRecord = bestPastSet != null && setItem.weight > bestPastSet.weight
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DuolingoGreen, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "Serie ${sIdx + 1}: ${setItem.weight} ${setItem.unit} × ${setItem.reps} reps (${setItem.durationSeconds}s)${if (!setItem.notes.isNullOrBlank()) " [${setItem.notes}]" else ""}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isNewRecord) {
                                                Surface(shape = RoundedCornerShape(6.dp), color = DuolingoYellow) {
                                                    Text("🏆 RÉCORD", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }

                                        IconButton(onClick = {
                                            haptics.performClick()
                                            val updatedSets = exItem.sets.filterIndexed { i, _ -> i != sIdx }
                                            currentExercises = currentExercises.map {
                                                if (it.exerciseId == exItem.exerciseId) it.copy(sets = updatedSets) else it
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = DuolingoRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Add Set Form Row
                        var setWeightInput by remember { mutableStateOf("60") }
                        var setRepsInput by remember { mutableStateOf("10") }
                        var setNotesInput by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = setWeightInput,
                                onValueChange = { setWeightInput = it },
                                label = { Text(selectedUnit) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = setRepsInput,
                                onValueChange = { setRepsInput = it },
                                label = { Text("Reps") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = setNotesInput,
                                onValueChange = { setNotesInput = it },
                                label = { Text("Nota") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Set Actions (Timer & Add Button)
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
                                    val w = setWeightInput.toFloatOrNull() ?: 0f
                                    val r = setRepsInput.toIntOrNull() ?: 0
                                    if (w > 0 && r > 0) {
                                        haptics.performSuccess()
                                        val newSet = ActiveSetItem(
                                            setNumber = exItem.sets.size + 1,
                                            weight = w,
                                            reps = r,
                                            unit = selectedUnit,
                                            durationSeconds = setTimerSeconds,
                                            notes = setNotesInput.ifBlank { null },
                                            isGhost = false
                                        )
                                        currentExercises = currentExercises.map {
                                            if (it.exerciseId == exItem.exerciseId) {
                                                it.copy(sets = it.sets + newSet)
                                            } else it
                                        }
                                        isSetTimerRunning = false
                                        setTimerSeconds = 0L
                                        setNotesInput = ""
                                        restSecondsRemaining = restDurationSetting
                                        isRestTimerRunning = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+ Guardar Serie", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Session Bottom Control Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DuolingoButton(
                        text = "Añadir Ejercicio",
                        onClick = { showSelectExerciseDialog = true },
                        containerColor = DuolingoBlue,
                        shadowColor = DuolingoBlueDark,
                        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                    )

                    DuolingoButton(
                        text = "✓ Finalizar y Celebrar",
                        onClick = {
                            val setsToSave = mutableListOf<WorkoutSetEntity>()
                            var setCounter = 1
                            var recordsCount = 0

                            currentExercises.forEach { exData ->
                                val pastBest = workoutsWithSetsState.flatMap { it.sets }.filter { it.exerciseId == exData.exerciseId }.maxByOrNull { it.weight }
                                exData.sets.filter { !it.isGhost }.forEach { setItem ->
                                    if (pastBest != null && setItem.weight > pastBest.weight) recordsCount++
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
                                ex.sets.filter { !it.isGhost }.sumOf { (w, r) -> (w * r).toDouble() }
                            }.toInt()

                            completedTotalVolume = totalVol
                            completedTotalSets = setsToSave.size
                            completedDuration = elapsedSeconds
                            newRecordsBrokenCount = recordsCount

                            viewModel.saveWorkoutSession(
                                title = activeWorkoutTitle,
                                startTime = sessionStartTime,
                                durationSeconds = elapsedSeconds,
                                notes = "Volumen: $totalVol $selectedUnit • ${setsToSave.size} series • Routine:${activeRoutineId ?: 0}",
                                sets = setsToSave
                            )

                            isWorkoutActive = false
                            isRestTimerRunning = false
                            isSetTimerRunning = false
                            showCelebrationSummary = true
                            currentExercises = emptyList()
                        },
                        containerColor = DuolingoGreen,
                        shadowColor = DuolingoGreenDark
                    )
                }
            }
        } else {
            // NO ACTIVE WORKOUT LANDING PANEL

            // Hero Card: Scheduled Today's Routine
            if (todayRoutineItem != null) {
                item {
                    val r = todayRoutineItem.routine
                    DuolingoCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(8.dp), color = DuolingoGreen) {
                                    Text("PROGRAMADA HOY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }

                            Text(text = r.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Día $todayCode • ${todayRoutineItem.items.size} ejercicios programados", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                            DuolingoButton(
                                text = "⚡ Iniciar Rutina de Hoy",
                                onClick = {
                                    haptics.performSuccess()
                                    activeWorkoutTitle = r.name
                                    activeRoutineId = r.id
                                    isWorkoutActive = true
                                    sessionStartTime = System.currentTimeMillis()
                                    elapsedSeconds = 0L

                                    val exerciseMap = allExercisesState.associateBy { it.id }
                                    currentExercises = todayRoutineItem.items.sortedBy { it.orderIndex }.mapNotNull { item ->
                                        val ex = exerciseMap[item.exerciseId]
                                        if (ex != null) loadExerciseWithHistory(ex.id, ex.name, ex.muscleGroup, ex.localPhotoPath, r.id) else null
                                    }
                                },
                                containerColor = DuolingoGreen,
                                shadowColor = DuolingoGreenDark
                            )
                        }
                    }
                }
            } else {
                item {
                    DuolingoCard {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text("🛋️ Día de Descanso Programado", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No tienes rutinas agendadas para hoy. Puedes descansar o iniciar una sesión libre.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Quick Routine Launcher Carousel / List
            item {
                Text("Tus Rutinas Configuradas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (routinesState.isEmpty()) {
                item {
                    Text("No tienes rutinas creadas. Puedes crear una en la pestaña 'Rutinas'.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                items(routinesState) { rItem ->
                    val r = rItem.routine
                    DuolingoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = r.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${rItem.items.size} ejercicios • Días: ${r.daysOfWeek}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Button(
                                onClick = {
                                    haptics.performSuccess()
                                    activeWorkoutTitle = r.name
                                    activeRoutineId = r.id
                                    isWorkoutActive = true
                                    sessionStartTime = System.currentTimeMillis()
                                    elapsedSeconds = 0L

                                    val exerciseMap = allExercisesState.associateBy { it.id }
                                    currentExercises = rItem.items.sortedBy { it.orderIndex }.mapNotNull { item ->
                                        val ex = exerciseMap[item.exerciseId]
                                        if (ex != null) loadExerciseWithHistory(ex.id, ex.name, ex.muscleGroup, ex.localPhotoPath, r.id) else null
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Iniciar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Free Workout Button
            item {
                DuolingoButton(
                    text = "💪 Nuevo Entrenamiento Libre",
                    onClick = {
                        haptics.performSuccess()
                        activeWorkoutTitle = "Entrenamiento Libre"
                        activeRoutineId = null
                        isWorkoutActive = true
                        sessionStartTime = System.currentTimeMillis()
                        elapsedSeconds = 0L
                        val firstEx = allExercisesState.firstOrNull()
                        currentExercises = if (firstEx != null) {
                            listOf(loadExerciseWithHistory(firstEx.id, firstEx.name, firstEx.muscleGroup, firstEx.localPhotoPath, null))
                        } else emptyList()
                    },
                    containerColor = DuolingoBlue,
                    shadowColor = DuolingoBlueDark
                )
            }
        }
    }

    // Celebration Modal
    if (showCelebrationSummary) {
        AlertDialog(
            onDismissRequest = { showCelebrationSummary = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = DuolingoYellow, modifier = Modifier.size(32.dp))
                    Text("¡Entrenamiento Completado!", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎉 ¡Excelente trabajo! Has registrado tu sesión en Room y sincronizado con la nube.", fontSize = 13.sp)
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
                            Text("🔥 Volumen Total: $completedTotalVolume $selectedUnit", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DuolingoGreen)
                            if (newRecordsBrokenCount > 0) {
                                Text("🏆 Récords Personales Rotos: $newRecordsBrokenCount", fontWeight = FontWeight.Black, fontSize = 14.sp, color = DuolingoYellow)
                            }
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

    // Exercise Selection Modal
    if (showSelectExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showSelectExerciseDialog = false },
            title = { Text("Añadir Ejercicio", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    OutlinedTextField(
                        value = exerciseSearchQuery,
                        onValueChange = { exerciseSearchQuery = it },
                        label = { Text("Buscar ejercicio...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val filteredExercises = allExercisesState.filter {
                        (exerciseSearchQuery.isBlank() || it.name.contains(exerciseSearchQuery, ignoreCase = true) || it.muscleGroup.contains(exerciseSearchQuery, ignoreCase = true)) &&
                        (selectedFilterMuscleGroup == "Todos" || it.muscleGroup.equals(selectedFilterMuscleGroup, ignoreCase = true))
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
                                            val newEx = loadExerciseWithHistory(exItem.id, exItem.name, exItem.muscleGroup, exItem.localPhotoPath, activeRoutineId)
                                            currentExercises = currentExercises + newEx
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
}
}
}
}
