package com.titoodev.h_gym.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titoodev.h_gym.ui.components.rememberHGymHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationWizardScreen(
    initialName: String = "",
    initialEmail: String = "",
    onRegistrationCompleted: (
        name: String,
        age: Int,
        sex: String,
        height: Float,
        weight: Float,
        goals: List<String>,
        experienceLevel: String,
        frequency: Int,
        trainingType: String,
        chest: Float?,
        waist: Float?,
        hips: Float?,
        arm: Float?,
        thigh: Float?,
        bodyFat: Float?
    ) -> Unit
) {
    val haptics = rememberHGymHaptics()
    var step by remember { mutableIntStateOf(1) }

    // Step 1: Personal Info
    var name by remember { mutableStateOf(initialName.ifBlank { "Atleta" }) }
    var ageText by remember { mutableStateOf("25") }
    var sex by remember { mutableStateOf("No especificar") }
    var heightText by remember { mutableStateOf("175") }
    var weightText by remember { mutableStateOf("70.0") }

    // Step 2: Sports Info
    val availableGoals = listOf("Ganar masa muscular", "Perder grasa", "Ganar fuerza", "Mejorar resistencia")
    var selectedGoals by remember { mutableStateOf(setOf("Ganar masa muscular")) }
    var experienceLevel by remember { mutableStateOf("Intermedio") }
    var weeklyFrequency by remember { mutableIntStateOf(4) }
    var preferredTrainingType by remember { mutableStateOf("Fuerza / Hipertrofia") }

    // Step 3: Body Measurements (Optional)
    var chestText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var hipsText by remember { mutableStateOf("") }
    var armText by remember { mutableStateOf("") }
    var thighText by remember { mutableStateOf("") }
    var bodyFatText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Perfil ($step/3)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
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
                            1 -> "Información Personal"
                            2 -> "Información Deportiva"
                            else -> "Medidas Corporales (Opcional)"
                        },
                        fontSize = 22.sp,
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
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Nombre completo") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = ageText,
                                        onValueChange = { ageText = it },
                                        label = { Text("Edad (años)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = heightText,
                                        onValueChange = { heightText = it },
                                        label = { Text("Altura (cm)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = weightText,
                                        onValueChange = { weightText = it },
                                        label = { Text("Peso actual (kg)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Text("Sexo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Masculino", "Femenino", "No especificar").forEach { option ->
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
                                    Text("Objetivos Principales (Múltiple)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Checkbox(
                                                checked = selectedGoals.contains(goal),
                                                onCheckedChange = null
                                            )
                                            Text(text = goal, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nivel de Experiencia", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Principiante", "Intermedio", "Avanzado").forEach { level ->
                                            FilterChip(
                                                selected = experienceLevel == level,
                                                onClick = {
                                                    haptics.performClick()
                                                    experienceLevel = level
                                                },
                                                label = { Text(level) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Frecuencia Semanal: $weeklyFrequency días/semana", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Slider(
                                        value = weeklyFrequency.toFloat(),
                                        onValueChange = { weeklyFrequency = it.toInt() },
                                        valueRange = 1f..7f,
                                        steps = 5
                                    )
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
                                    Text("Medidas de Referencia (cm)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    OutlinedTextField(
                                        value = chestText,
                                        onValueChange = { chestText = it },
                                        label = { Text("Pecho (cm)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = waistText,
                                        onValueChange = { waistText = it },
                                        label = { Text("Cintura (cm)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = hipsText,
                                        onValueChange = { hipsText = it },
                                        label = { Text("Cadera (cm)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = armText,
                                        onValueChange = { armText = it },
                                        label = { Text("Brazo (cm)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = bodyFatText,
                                        onValueChange = { bodyFatText = it },
                                        label = { Text("% Grasa Corporal") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
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
                            .height(52.dp),
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
                            onRegistrationCompleted(
                                name,
                                ageText.toIntOrNull() ?: 25,
                                sex,
                                heightText.toFloatOrNull() ?: 175f,
                                weightText.toFloatOrNull() ?: 70f,
                                selectedGoals.toList(),
                                experienceLevel,
                                weeklyFrequency,
                                preferredTrainingType,
                                chestText.toFloatOrNull(),
                                waistText.toFloatOrNull(),
                                hipsText.toFloatOrNull(),
                                armText.toFloatOrNull(),
                                thighText.toFloatOrNull(),
                                bodyFatText.toFloatOrNull()
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (step < 3) "Siguiente" else "Completar Registro")
                }
            }
        }
    }
}
