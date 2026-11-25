package com.titoodev.h_gym.ui.screens.routines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.RoutineWithItems
import com.titoodev.h_gym.ui.components.*
import com.titoodev.h_gym.ui.screens.workout.WorkoutViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    onStartRoutine: (Long) -> Unit = {},
    viewModel: WorkoutViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()

    val routinesWithItems by viewModel.routinesWithItemsState.collectAsState()
    val allExercises by viewModel.allExercisesState.collectAsState()

    var showRoutineDialog by remember { mutableStateOf(false) }
    var editingRoutineId by remember { mutableLongStateOf(0L) }
    var routineNameInput by remember { mutableStateOf("") }

    val allDays = listOf("L", "M", "X", "J", "V", "S", "D")
    var selectedDays by remember { mutableStateOf(setOf("L", "M", "X", "J", "V")) }
    var selectedExerciseIds by remember { mutableStateOf(listOf<Long>()) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineWithItems?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Rutinas Configuradas (${routinesWithItems.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (routinesWithItems.isEmpty()) {
                item {
                    DuolingoCard {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "💡 No tienes rutinas creadas aún",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Crea tu primera rutina semanal seleccionando los días y los ejercicios que deseas hacer.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(routinesWithItems) { rItem ->
                    val r = rItem.routine
                    val assignedDays = remember(r.daysOfWeek) { r.daysOfWeek.split(",").map { it.trim() } }

                    DuolingoCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = r.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "${rItem.items.size} ejercicio(s)", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            haptics.performClick()
                                            editingRoutineId = r.id
                                            routineNameInput = r.name
                                            selectedDays = assignedDays.toSet()
                                            selectedExerciseIds = rItem.items.sortedBy { it.orderIndex }.map { it.exerciseId }
                                            showRoutineDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = {
                                            haptics.performClick()
                                            routineToDelete = rItem
                                            showDeleteConfirmDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Day Chips Bar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allDays.forEach { day ->
                                    val isDayActive = assignedDays.contains(day)
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isDayActive) DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDayActive) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            DuolingoButton(
                                text = "Iniciar Entrenamiento",
                                onClick = {
                                    haptics.performSuccess()
                                    onStartRoutine(r.id)
                                },
                                containerColor = DuolingoGreen,
                                shadowColor = DuolingoGreenDark
                            )

                            // Exercises List Preview
                            if (rItem.items.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                val exerciseMap = remember(allExercises) { allExercises.associateBy { it.id } }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rItem.items.sortedBy { it.orderIndex }.forEachIndexed { idx, item ->
                                        val ex = exerciseMap[item.exerciseId]
                                        if (ex != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (ex.localPhotoPath != null) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(File(ex.localPhotoPath)),
                                                        contentDescription = ex.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                } else {
                                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                                Text(text = "${idx + 1}. ${ex.name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    haptics.performSuccess()
                    editingRoutineId = 0L
                    routineNameInput = ""
                    selectedDays = setOf("L", "M", "X", "J", "V")
                    selectedExerciseIds = allExercises.take(3).map { it.id }
                    showRoutineDialog = true
                },
                containerColor = DuolingoGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Rutina")
            }
        }
    }

    // Create / Edit Routine Dialog
    if (showRoutineDialog) {
        AlertDialog(
            onDismissRequest = { showRoutineDialog = false },
            title = { Text(if (editingRoutineId == 0L) "Crear Rutina Semanal" else "Editar Rutina", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.heightIn(max = 440.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = routineNameInput,
                            onValueChange = { routineNameInput = it },
                            label = { Text("Nombre de la rutina (ej. Push Día A)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Text("Días de la semana para esta rutina:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            allDays.forEach { day ->
                                val isSel = selectedDays.contains(day)
                                DuolingoDayChip(
                                    dayLabel = day,
                                    isSelected = isSel,
                                    onToggle = {
                                        selectedDays = if (isSel) selectedDays - day else selectedDays + day
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Text("Selecciona los ejercicios (Selección múltiple):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    items(allExercises) { ex ->
                        val isExSelected = selectedExerciseIds.contains(ex.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isExSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    if (isExSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    haptics.performClick()
                                    selectedExerciseIds = if (isExSelected) {
                                        selectedExerciseIds - ex.id
                                    } else {
                                        selectedExerciseIds + ex.id
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (ex.localPhotoPath != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(File(ex.localPhotoPath)),
                                            contentDescription = ex.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text(text = ex.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = ex.muscleGroup, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }

                                if (isExSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                DuolingoButton(
                    text = "Guardar Rutina",
                    onClick = {
                        if (routineNameInput.isNotBlank() && selectedDays.isNotEmpty()) {
                            val daysStr = allDays.filter { selectedDays.contains(it) }.joinToString(",")
                            viewModel.saveFullRoutine(
                                id = editingRoutineId,
                                name = routineNameInput,
                                daysOfWeek = daysStr,
                                exerciseIds = selectedExerciseIds
                            )
                            showRoutineDialog = false
                        }
                    },
                    modifier = Modifier.width(160.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showRoutineDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Routine Confirmation Dialog
    if (showDeleteConfirmDialog && routineToDelete != null) {
        val r = routineToDelete!!.routine
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar Rutina?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Vas a eliminar la rutina '${r.name}'. Los registros de entrenamientos ya realizados en el pasado NO se borrarán.")
            },
            confirmButton = {
                DuolingoButton(
                    text = "Eliminar",
                    onClick = {
                        viewModel.deleteRoutine(r.id)
                        showDeleteConfirmDialog = false
                        routineToDelete = null
                    },
                    containerColor = DuolingoRed,
                    shadowColor = DuolingoRedDark,
                    modifier = Modifier.width(130.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
