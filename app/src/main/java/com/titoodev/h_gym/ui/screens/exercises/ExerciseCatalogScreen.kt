package com.titoodev.h_gym.ui.screens.exercises

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.ui.components.DuolingoButton
import com.titoodev.h_gym.ui.components.DuolingoCard
import com.titoodev.h_gym.ui.components.DuolingoGreen
import com.titoodev.h_gym.ui.components.DuolingoRed
import com.titoodev.h_gym.ui.components.DuolingoRedDark
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import com.titoodev.h_gym.util.ImageUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(
    viewModel: ExerciseCatalogViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exercisesState by viewModel.exercisesState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingExerciseId by remember { mutableLongStateOf(0L) }

    var exerciseNameInput by remember { mutableStateOf("") }
    var exerciseDescInput by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("Pecho") }
    var selectedEquipment by remember { mutableStateOf("Barra") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var isNameError by remember { mutableStateOf(false) }

    // Deletion Confirmation Dialog State
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseEntity?>(null) }
    var routinesInUseCount by remember { mutableIntStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedPhotoUri = tempCameraUri
        }
    }

    val muscleGroups = listOf("Pecho", "Espalda", "Piernas", "Hombros", "Brazos", "Core", "General")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Tus Ejercicios (${exercisesState.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (exercisesState.isEmpty()) {
                item {
                    DuolingoCard {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "💡 Aún no tienes ejercicios en tu biblioteca",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pulsa el botón + verde para crear tu primer ejercicio con foto y descripción.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(exercisesState) { exercise ->
                    DuolingoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (exercise.localPhotoPath != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(exercise.localPhotoPath)),
                                        contentDescription = exercise.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Column {
                                    Text(text = exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "${exercise.muscleGroup} • ${exercise.equipment ?: "Libre"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    if (!exercise.description.isNullOrBlank()) {
                                        Text(text = exercise.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2)
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        haptics.performClick()
                                        editingExerciseId = exercise.id
                                        exerciseNameInput = exercise.name
                                        exerciseDescInput = exercise.description ?: ""
                                        selectedMuscleGroup = exercise.muscleGroup
                                        selectedEquipment = exercise.equipment ?: "Libre"
                                        selectedPhotoUri = null
                                        isNameError = false
                                        showEditDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performClick()
                                        exerciseToDelete = exercise
                                        coroutineScope.launch {
                                            routinesInUseCount = viewModel.getRoutinesCountForExercise(exercise.id)
                                            showDeleteConfirmationDialog = true
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
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
                    editingExerciseId = 0L
                    exerciseNameInput = ""
                    exerciseDescInput = ""
                    selectedMuscleGroup = "Pecho"
                    selectedEquipment = "Barra"
                    selectedPhotoUri = null
                    isNameError = false
                    showEditDialog = true
                },
                containerColor = DuolingoGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Ejercicio")
            }
        }
    }

    // Add / Edit Exercise Dialog with Validations & Photo Pickers
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingExerciseId == 0L) "Crear Nuevo Ejercicio" else "Editar Ejercicio",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = exerciseNameInput,
                            onValueChange = {
                                exerciseNameInput = it
                                isNameError = it.isBlank()
                            },
                            label = { Text("Nombre del ejercicio *") },
                            isError = isNameError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (isNameError) {
                            Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = exerciseDescInput,
                            onValueChange = { exerciseDescInput = it },
                            label = { Text("Descripción / Técnica (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (exerciseDescInput.isBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Se recomienda añadir una breve descripción para recordar la técnica", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item {
                        Text("Grupo Muscular Principal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            muscleGroups.take(4).forEach { mg ->
                                FilterChip(
                                    selected = selectedMuscleGroup == mg,
                                    onClick = {
                                        haptics.performClick()
                                        selectedMuscleGroup = mg
                                    },
                                    label = { Text(mg, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Foto del Ejercicio (Cámara o Galería)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    haptics.performClick()
                                    val pair = ImageUtils.createTempCameraUri(context)
                                    if (pair != null) {
                                        tempCameraUri = pair.second
                                        cameraLauncher.launch(pair.second)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cámara", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    haptics.performClick()
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Galería", fontSize = 12.sp)
                            }
                        }

                        if (selectedPhotoUri == null) {
                            Text("Se recomienda incluir foto para identificarlo rápidamente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        } else {
                            Text("✓ Foto seleccionada", fontSize = 12.sp, color = DuolingoGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                DuolingoButton(
                    text = "Guardar",
                    onClick = {
                        if (exerciseNameInput.isBlank()) {
                            isNameError = true
                        } else {
                            viewModel.saveExercise(
                                id = editingExerciseId,
                                name = exerciseNameInput,
                                description = exerciseDescInput.ifBlank { null },
                                photoUri = selectedPhotoUri,
                                muscleGroup = selectedMuscleGroup,
                                equipment = selectedEquipment
                            )
                            showEditDialog = false
                        }
                    },
                    modifier = Modifier.width(130.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Confirmation Dialog if Exercise is in Routine
    if (showDeleteConfirmationDialog && exerciseToDelete != null) {
        val ex = exerciseToDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("¿Eliminar Ejercicio?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vas a eliminar el ejercicio '${ex.name}'.")
                    if (routinesInUseCount > 0) {
                        Text(
                            text = "⚠️ ATENCIÓN: Este ejercicio forma parte de $routinesInUseCount rutina(s) activa(s). Se eliminará del catálogo pero tus registros de entrenamientos pasados NO se perderán.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text("Esta acción no se puede deshacer.")
                    }
                }
            },
            confirmButton = {
                DuolingoButton(
                    text = "Eliminar",
                    onClick = {
                        viewModel.deleteExercise(ex.id)
                        showDeleteConfirmationDialog = false
                        exerciseToDelete = null
                    },
                    containerColor = DuolingoRed,
                    shadowColor = DuolingoRedDark,
                    modifier = Modifier.width(130.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
