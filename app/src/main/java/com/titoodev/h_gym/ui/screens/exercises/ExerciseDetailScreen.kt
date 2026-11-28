package com.titoodev.h_gym.ui.screens.exercises

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import com.titoodev.h_gym.domain.usecase.Calculate1RMUseCase
import com.titoodev.h_gym.domain.usecase.OneRMFormula
import com.titoodev.h_gym.ui.components.DuolingoCard
import com.titoodev.h_gym.ui.components.DuolingoGreen
import com.titoodev.h_gym.ui.components.DuolingoYellow
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExerciseDetailScreen(
    exerciseId: Long
) {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    val db = remember { HGymDatabase.getDatabase(context) }
    val calculate1RMUseCase = remember { Calculate1RMUseCase() }

    var selectedFormula by remember { mutableStateOf(OneRMFormula.EPLEY) }

    val exerciseState by db.exerciseDao().getAllExercises().collectAsState(initial = emptyList())
    val exercise = exerciseState.find { it.id == exerciseId } ?: ExerciseEntity(id = exerciseId, name = "Ejercicio", muscleGroup = "General")

    val setsHistoryState by db.exerciseDao().getSetsHistoryForExercise(exerciseId).collectAsState(initial = emptyList())

    val bestWeight = remember(setsHistoryState) { setsHistoryState.maxOfOrNull { it.weight } ?: 0f }
    val bestVolumeSet = remember(setsHistoryState) { setsHistoryState.maxByOrNull { it.weight * it.reps } }
    val best1RM = remember(setsHistoryState, selectedFormula) {
        setsHistoryState.maxOfOrNull {
            calculate1RMUseCase(it.weight, it.reps, selectedFormula)
        } ?: 0f
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Exercise Header Card
        item {
            DuolingoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = exercise.name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(text = exercise.muscleGroup, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        if (!exercise.equipment.isNullOrBlank()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(text = exercise.equipment!!, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                    if (!exercise.description.isNullOrBlank()) {
                        Text(text = exercise.description!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Personal Bests Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Peso Máx", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${String.format(Locale.getDefault(), "%.1f", bestWeight)} kg", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DuolingoGreen)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("1RM Estimado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${String.format(Locale.getDefault(), "%.1f", best1RM)} kg", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DuolingoYellow)
                    }
                }
            }
        }

        // Formula Selector & Estimation Disclaimer Card
        item {
            DuolingoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fórmula de 1RM (Estimación)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OneRMFormula.values().forEach { formula ->
                                FilterChip(
                                    selected = selectedFormula == formula,
                                    onClick = {
                                        haptics.performClick()
                                        selectedFormula = formula
                                    },
                                    label = { Text(formula.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = primaryColor)
                        Text("El 1RM es una estimación matemática teórica y no constituye una recomendación de carga directa.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Progression Chart Card
        if (setsHistoryState.isNotEmpty()) {
            item {
                DuolingoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Evolución Histórica de Carga", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        val sortedSets = remember(setsHistoryState) { setsHistoryState.sortedBy { it.id } }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(vertical = 12.dp)
                        ) {
                            val maxVal = (sortedSets.maxOfOrNull { it.weight } ?: 1f).coerceAtLeast(1f)
                            val minVal = (sortedSets.minOfOrNull { it.weight } ?: 0f)
                            val range = (maxVal - minVal).coerceAtLeast(1f)

                            val stepX = size.width / (sortedSets.size - 1).coerceAtLeast(1)
                            val points = sortedSets.mapIndexed { idx, item ->
                                val x = idx * stepX
                                val normalizedY = (item.weight - minVal) / range
                                val y = size.height - (normalizedY * (size.height * 0.8f)) - (size.height * 0.1f)
                                Offset(x, y)
                            }

                            if (points.size >= 2) {
                                val strokePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                drawPath(strokePath, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            }

                            points.forEach { point ->
                                drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = point)
                            }
                        }
                    }
                }
            }

            item {
                Text("Historial Completo de Series (${setsHistoryState.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            items(setsHistoryState) { setItem ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Serie ${setItem.setNumber}: ${setItem.weight} ${setItem.unit} × ${setItem.reps} reps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val e1rm = calculate1RMUseCase(setItem.weight, setItem.reps, selectedFormula)
                            Text("1RM Teórico: ${String.format(Locale.getDefault(), "%.1f", e1rm)} kg${if (setItem.rirOrRpe != null) " • RPE ${setItem.rirOrRpe}" else ""}", fontSize = 12.sp, color = primaryColor)
                        }
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = primaryColor)
                    }
                }
            }
        }
    }
}
