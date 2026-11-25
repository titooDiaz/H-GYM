package com.titoodev.h_gym.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyMeasurementChartCard(
    title: String,
    unit: String,
    measurements: List<BodyMeasurementEntity>,
    selector: (BodyMeasurementEntity) -> Float?
) {
    val sorted = remember(measurements) { measurements.sortedBy { it.timestamp } }
    val dataPoints = remember(sorted) {
        sorted.mapNotNull { m ->
            val v = selector(m)
            if (v != null && v > 0f) Pair(m.timestamp, v) else null
        }
    }

    if (dataPoints.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val latest = dataPoints.last().second
            val first = dataPoints.first().second
            val diff = latest - first
            val diffStr = if (diff >= 0) "+${String.format(Locale.getDefault(), "%.1f", diff)} $unit" else "${String.format(Locale.getDefault(), "%.1f", diff)} $unit"
            val prev = if (dataPoints.size >= 2) dataPoints[dataPoints.size - 2].second else null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${String.format(Locale.getDefault(), "%.1f", latest)} $unit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progreso total: $diffStr (${dataPoints.size} registros)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (prev != null) {
                    Text(text = "Anterior: ${String.format(Locale.getDefault(), "%.1f", prev)} $unit", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                }
            }

            if (dataPoints.size >= 2) {
                Spacer(modifier = Modifier.height(14.dp))
                val values = dataPoints.map { it.second }
                val minV = values.minOrNull() ?: 0f
                val maxV = values.maxOrNull() ?: 100f
                val range = if (maxV - minV == 0f) 1f else maxV - minV

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 4.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val points = values.mapIndexed { idx, v ->
                            val x = (idx.toFloat() / (values.size - 1).coerceAtLeast(1)) * width
                            val y = height - (((v - minV) / range) * (height - 20.dp.toPx()) + 10.dp.toPx())
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
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()
    var selectedTab by remember { mutableIntStateOf(0) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    haptics.performClick()
                    selectedTab = 0
                },
                text = { Text("Mapa de Constancia", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    haptics.performClick()
                    selectedTab = 1
                },
                text = { Text("Evolución y Medidas", fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                ComplianceHeatmapScreen(viewModel = viewModel)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // 1. Real Body Weight Line Chart Card
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
                            val prevWeight = sortedMeasurements[sortedMeasurements.size - 2].weight

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progreso total: $diffText (${sortedMeasurements.size} registros)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Anterior: $prevWeight kg",
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Canvas Line Chart
                            val weights = sortedMeasurements.map { it.weight }
                            val minW = weights.minOrNull() ?: 50f
                            val maxW = weights.maxOrNull() ?: 100f
                            val range = if (maxW - minW == 0f) 1f else maxW - minW

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 4.dp)
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
                                            radius = 4.dp.toPx(),
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

            // 2. Body Measurements Evolution Charts (Waist, Chest, Arm, Thigh, Body Fat)
            item {
                BodyMeasurementChartCard(
                    title = "Evolución de Cintura",
                    unit = "cm",
                    measurements = allMeasurements,
                    selector = { it.waist }
                )
            }

            item {
                BodyMeasurementChartCard(
                    title = "Evolución de Pecho",
                    unit = "cm",
                    measurements = allMeasurements,
                    selector = { it.chest }
                )
            }

            item {
                BodyMeasurementChartCard(
                    title = "Evolución de Brazo",
                    unit = "cm",
                    measurements = allMeasurements,
                    selector = { it.arm }
                )
            }

            item {
                BodyMeasurementChartCard(
                    title = "Evolución de Muslo",
                    unit = "cm",
                    measurements = allMeasurements,
                    selector = { it.thigh }
                )
            }

            item {
                BodyMeasurementChartCard(
                    title = "Evolución de % Grasa Corporal",
                    unit = "%",
                    measurements = allMeasurements,
                    selector = { it.bodyFatPercentage }
                )
            }

            // 3. Real Estimated 1RM List Card
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

            // 4. Real Body Measurements Summary & Comparison Card
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
                                text = "Últimas Medidas vs. Anterior",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val sortedList = remember(allMeasurements) { allMeasurements.sortedBy { it.timestamp } }
                        val latest = sortedList.lastOrNull()
                        val previous = if (sortedList.size >= 2) sortedList[sortedList.size - 2] else null

                        if (latest != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            Text(
                                text = "Último registro (${sdf.format(Date(latest.timestamp))}):",
                                fontSize = 12.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            fun diffText(curr: Float?, prev: Float?, unit: String): String {
                                if (curr == null) return "-"
                                if (prev == null) return "${curr} $unit"
                                val d = curr - prev
                                val sign = if (d > 0) "+" else ""
                                return "${curr} $unit (${sign}${String.format(Locale.getDefault(), "%.1f", d)} $unit)"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "• Peso: ${diffText(latest.weight, previous?.weight, "kg")}", fontSize = 13.sp)
                                    Text(text = "• Cintura: ${diffText(latest.waist, previous?.waist, "cm")}", fontSize = 13.sp)
                                    Text(text = "• Pecho: ${diffText(latest.chest, previous?.chest, "cm")}", fontSize = 13.sp)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "• Brazo: ${diffText(latest.arm, previous?.arm, "cm")}", fontSize = 13.sp)
                                    Text(text = "• Muslo: ${diffText(latest.thigh, previous?.thigh, "cm")}", fontSize = 13.sp)
                                    Text(text = "• % Grasa: ${diffText(latest.bodyFatPercentage, previous?.bodyFatPercentage, "%")}", fontSize = 13.sp)
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

        if (selectedTab == 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        haptics.performSuccess()
                        weightInput = latestMeasurement?.weight?.toString() ?: "70.0"
                        chestInput = latestMeasurement?.chest?.toString() ?: ""
                        waistInput = latestMeasurement?.waist?.toString() ?: ""
                        armInput = latestMeasurement?.arm?.toString() ?: ""
                        thighInput = latestMeasurement?.thigh?.toString() ?: ""
                        fatInput = latestMeasurement?.bodyFatPercentage?.toString() ?: ""
                        showAddMeasurementDialog = true
                    },
                    containerColor = com.titoodev.h_gym.ui.components.DuolingoGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar Medidas")
                }
            }
        }

    // Add Measurement Dialog
    if (showAddMeasurementDialog) {
        AlertDialog(
            onDismissRequest = { showAddMeasurementDialog = false },
            title = { Text("Registrar Nuevo Peso y Medidas") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
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
                        label = { Text("Cintura (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Pecho (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = armInput,
                        onValueChange = { armInput = it },
                        label = { Text("Brazo (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = thighInput,
                        onValueChange = { thighInput = it },
                        label = { Text("Muslo (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = fatInput,
                        onValueChange = { fatInput = it },
                        label = { Text("% Grasa Corporal") },
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
}
}
}
