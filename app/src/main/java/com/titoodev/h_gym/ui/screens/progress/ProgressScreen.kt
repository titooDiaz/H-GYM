package com.titoodev.h_gym.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()

    val allMeasurements by viewModel.allMeasurementsState.collectAsState()
    val latestMeasurement by viewModel.latestMeasurementState.collectAsState()
    val exercise1RMList by viewModel.exercise1RMListState.collectAsState()

    var showAddMeasurementDialog by remember { mutableStateOf(false) }

    var weightInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var armInput by remember { mutableStateOf("") }
    var thighInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolución Física y Estadísticas", fontWeight = FontWeight.Bold) },
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
                    weightInput = latestMeasurement?.weight?.toString() ?: "70.0"
                    showAddMeasurementDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Medidas")
            }
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
            // Real Body Weight Line Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tendencia de Peso Corporal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = primaryColor)
                        }

                        val sortedMeasurements = remember(allMeasurements) {
                            allMeasurements.sortedBy { it.timestamp }
                        }

                        if (sortedMeasurements.size >= 2) {
                            val firstWeight = sortedMeasurements.first().weight
                            val lastWeight = sortedMeasurements.last().weight
                            val diff = lastWeight - firstWeight
                            val diffText = if (diff >= 0) "+${String.format(Locale.getDefault(), "%.1f", diff)} kg" else "${String.format(Locale.getDefault(), "%.1f", diff)} kg"

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Evolución acumulada: $diffText (${sortedMeasurements.size} registros)",
                                fontSize = 13.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Canvas Line Chart
                            val weights = sortedMeasurements.map { it.weight }
                            val minW = weights.minOrNull() ?: 50f
                            val maxW = weights.maxOrNull() ?: 100f
                            val range = if (maxW - minW == 0f) 1f else maxW - minW

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    val points = weights.mapIndexed { idx, w ->
                                        val x = (idx.toFloat() / (weights.size - 1).coerceAtLeast(1)) * width
                                        val y = height - (((w - minW) / range) * (height - 20.dp.toPx()) + 10.dp.toPx())
                                        Offset(x, y)
                                    }

                                    val path = Path().apply {
                                        points.forEachIndexed { i, pt ->
                                            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                        }
                                    }

                                    drawPath(
                                        path = path,
                                        color = primaryColor,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    points.forEach { pt ->
                                        drawCircle(
                                            color = primaryColor,
                                            radius = 5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }
                        } else if (sortedMeasurements.size == 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Peso actual: ${sortedMeasurements.first().weight} kg. Registra más mediciones con el botón + para ver tu gráfica de tendencia.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no has registrado tu peso. Pulsa + para comenzar tu historial.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Real Estimated 1RM List Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Récords de Fuerza Reales (1RM Estimado)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fórmula Epley: 1RM = Peso × (1 + Reps/30) calculado de tus entrenamientos en Room.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (exercise1RMList.isEmpty()) {
                            Text(
                                text = "Completa tus primeros entrenamientos para calcular automáticamente tus récords 1RM.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            exercise1RMList.forEach { info ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = info.exerciseName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "Mejor serie: ${info.maxWeight} kg × ${info.maxReps} reps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${info.estimated1RM.toInt()} kg 1RM",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real Body Measurements Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Medidas Corporales de Referencia",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val m = latestMeasurement
                        if (m != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            Text(
                                text = "Último registro (${sdf.format(Date(m.timestamp))}):",
                                fontSize = 12.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "• Peso: ${m.weight} kg", fontSize = 13.sp)
                                    Text(text = "• Cintura: ${m.waist ?: "-"} cm", fontSize = 13.sp)
                                    Text(text = "• Pecho: ${m.chest ?: "-"} cm", fontSize = 13.sp)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "• Brazo: ${m.arm ?: "-"} cm", fontSize = 13.sp)
                                    Text(text = "• Muslo: ${m.thigh ?: "-"} cm", fontSize = 13.sp)
                                    Text(text = "• % Grasa: ${m.bodyFatPercentage ?: "-"}%", fontSize = 13.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "No hay registros de medidas corporales. Haz clic en el botón + para registrar tu peso y medidas.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Measurement Dialog
    if (showAddMeasurementDialog) {
        AlertDialog(
            onDismissRequest = { showAddMeasurementDialog = false },
            title = { Text("Registrar Nuevo Peso y Medidas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso corporal (kg) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = waistInput,
                        onValueChange = { waistInput = it },
                        label = { Text("Cintura (cm) opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Pecho (cm) opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = armInput,
                        onValueChange = { armInput = it },
                        label = { Text("Brazo (cm) opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = fatInput,
                        onValueChange = { fatInput = it },
                        label = { Text("% Grasa Corporal opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = weightInput.toFloatOrNull() ?: 0f
                    if (w > 0) {
                        haptics.performSuccess()
                        viewModel.addMeasurement(
                            weight = w,
                            chest = chestInput.toFloatOrNull(),
                            waist = waistInput.toFloatOrNull(),
                            hips = null,
                            arm = armInput.toFloatOrNull(),
                            thigh = thighInput.toFloatOrNull(),
                            bodyFatPercentage = fatInput.toFloatOrNull()
                        )
                        showAddMeasurementDialog = false
                    }
                }) {
                    Text("Guardar en Historial")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMeasurementDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
