package com.titoodev.h_gym.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
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
import coil.compose.rememberAsyncImagePainter
import com.titoodev.h_gym.ui.components.DuolingoDayChip
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import com.titoodev.h_gym.util.ImageUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationWizardScreen(
    initialName: String = "",
    initialEmail: String = "",
    onRegistrationCompleted: (
        name: String,
        birthDateTimestamp: Long?,
        sex: String,
        height: Float?,
        weight: Float?,
        goals: List<String>,
        experienceLevel: String,
        trainingDays: String,
        preferredTrainingType: String,
        photoUri: Uri?,
        chest: Float?,
        waist: Float?,
        hips: Float?,
        arm: Float?,
        forearm: Float?,
        thigh: Float?,
        calf: Float?,
        bodyFat: Float?
    ) -> Unit
) {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }

    // Photo
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) selectedPhotoUri = tempCameraUri
    }

    // Step 1: Personal Info
    var name by remember { mutableStateOf(initialName.ifBlank { "Atleta" }) }
    var birthYearText by remember { mutableStateOf("1998") }
    var sex by remember { mutableStateOf("No especificar") }
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    // Step 2: Sports Info
    val availableGoals = listOf("Ganar masa muscular", "Perder grasa", "Ganar fuerza", "Mejorar resistencia")
    var selectedGoals by remember { mutableStateOf(setOf("Ganar masa muscular")) }
    var experienceLevel by remember { mutableStateOf("Intermedio") }
    val allDays = listOf("L", "M", "X", "J", "V", "S", "D")
    var selectedDays by remember { mutableStateOf(setOf("L", "M", "X", "J", "V")) }
    var preferredTrainingType by remember { mutableStateOf("Hipertrofia") }

    // Step 3: Body Measurements (Optional)
    var chestText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var hipsText by remember { mutableStateOf("") }
    var armText by remember { mutableStateOf("") }
    var forearmText by remember { mutableStateOf("") }
    var thighText by remember { mutableStateOf("") }
    var calfText by remember { mutableStateOf("") }
    var bodyFatText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = when (step) {
                        1 -> "Paso 1: Perfil e Información Personal"
                        2 -> "Paso 2: Preferencias Deportivas"
                        else -> "Paso 3: Medidas Corporales (Opcional)"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when (step) {
                1 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Profile Photo Picker
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedPhotoUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(selectedPhotoUri),
                                            contentDescription = "Foto de perfil",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            haptics.performClick()
                                            val pair = ImageUtils.createTempCameraUri(context)
                                            if (pair != null) {
                                                tempCameraUri = pair.second
                                                cameraLauncher.launch(pair.second)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cámara", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            haptics.performClick()
                                            galleryLauncher.launch("image/*")
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Galería", fontSize = 12.sp)
                                    }
                                }

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nombre completo") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = birthYearText,
                                    onValueChange = { birthYearText = it },
                                    label = { Text("Año de nacimiento (ej. 1998)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = heightText,
                                    onValueChange = { heightText = it },
                                    label = { Text("Altura (cm, opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = { weightText = it },
                                    label = { Text("Peso inicial (kg, opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Text("Sexo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Start))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf("Masculino", "Femenino", "Otro").forEach { option ->
                                        FilterChip(
                                            selected = sex == option,
                                            onClick = {
                                                haptics.performClick()
                                                sex = option
                                            },
                                            label = { Text(option) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("Días Habituales de Entrenamiento", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Estilo de Entrenamiento Preferido", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf("Fuerza", "Hipertrofia", "Calistenia", "Híbrido").forEach { style ->
                                        FilterChip(
                                            selected = preferredTrainingType == style,
                                            onClick = {
                                                haptics.performClick()
                                                preferredTrainingType = style
                                            },
                                            label = { Text(style, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Objetivos Principales", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                availableGoals.forEach { goal ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptics.performClick()
                                                selectedGoals = if (selectedGoals.contains(goal)) {
                                                    selectedGoals - goal
                                                } else {
                                                    selectedGoals + goal
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(checked = selectedGoals.contains(goal), onCheckedChange = null)
                                        Text(text = goal, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Medidas Iniciales Opcionales (cm)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = chestText, onValueChange = { chestText = it }, label = { Text("Pecho") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                    OutlinedTextField(value = waistText, onValueChange = { waistText = it }, label = { Text("Cintura") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = hipsText, onValueChange = { hipsText = it }, label = { Text("Cadera") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                    OutlinedTextField(value = armText, onValueChange = { armText = it }, label = { Text("Brazo") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = forearmText, onValueChange = { forearmText = it }, label = { Text("Antebrazo") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                    OutlinedTextField(value = thighText, onValueChange = { thighText = it }, label = { Text("Muslo") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = calfText, onValueChange = { calfText = it }, label = { Text("Pantorrilla") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                    OutlinedTextField(value = bodyFatText, onValueChange = { bodyFatText = it }, label = { Text("% Grasa") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Actions Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = {
                        haptics.performClick()
                        step--
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Anterior")
                }
            }

            Button(
                onClick = {
                    haptics.performSuccess()
                    if (step < 3) {
                        step++
                    } else {
                        val birthYear = birthYearText.toIntOrNull() ?: 1998
                        val birthTimestamp = Calendar.getInstance().apply {
                            set(Calendar.YEAR, birthYear)
                            set(Calendar.MONTH, Calendar.JANUARY)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.timeInMillis

                        val daysStr = allDays.filter { selectedDays.contains(it) }.joinToString(",")

                        onRegistrationCompleted(
                            name,
                            birthTimestamp,
                            sex,
                            heightText.toFloatOrNull(),
                            weightText.toFloatOrNull(),
                            selectedGoals.toList(),
                            experienceLevel,
                            daysStr,
                            preferredTrainingType,
                            selectedPhotoUri,
                            chestText.toFloatOrNull(),
                            waistText.toFloatOrNull(),
                            hipsText.toFloatOrNull(),
                            armText.toFloatOrNull(),
                            forearmText.toFloatOrNull(),
                            thighText.toFloatOrNull(),
                            calfText.toFloatOrNull(),
                            bodyFatText.toFloatOrNull()
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (step < 3) "Siguiente" else "Completar Registro")
            }
        }
    }
}
