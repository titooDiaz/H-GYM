package com.titoodev.h_gym.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titoodev.h_gym.ui.components.*
import com.titoodev.h_gym.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

enum class ComplianceDayStatus {
    GREEN,          // Routine scheduled & completed
    RED,            // Routine scheduled & NOT completed (past day)
    GRAY,           // Rest day (no routine scheduled)
    NEUTRAL_TODAY,  // Today with routine pending
    BLANK           // Before account registration / first record (completely empty/blank)
}

data class DayComplianceInfo(
    val dateString: String,
    val timestamp: Long,
    val status: ComplianceDayStatus,
    val scheduledCount: Int,
    val completedCount: Int,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceHeatmapScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()

    val workoutsWithSets by viewModel.workoutsWithSetsState.collectAsState()

    var selectedDayInfo by remember { mutableStateOf<DayComplianceInfo?>(null) }
    var showDayDetailDialog by remember { mutableStateOf(false) }

    val todayTimestamp = remember { DateUtils.getStartOfDay(System.currentTimeMillis()) }

    // First record timestamp
    val firstRecordTime = remember(workoutsWithSets) {
        val wMin = workoutsWithSets.minOfOrNull { it.workout.date } ?: Long.MAX_VALUE
        if (wMin == Long.MAX_VALUE) DateUtils.getStartOfDay(System.currentTimeMillis()) else DateUtils.getStartOfDay(wMin)
    }

    // Generate 60 days matrix history (approx 2 months)
    val daysList = remember(workoutsWithSets, firstRecordTime) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = todayTimestamp
            add(Calendar.DAY_OF_YEAR, -59)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val result = mutableListOf<DayComplianceInfo>()

        for (i in 0 until 60) {
            val dayStart = DateUtils.getStartOfDay(cal.timeInMillis)
            val dayEnd = DateUtils.getEndOfDay(cal.timeInMillis)
            val dayCode = DateUtils.getCurrentDayOfWeekCode(cal.timeInMillis)
            val isToday = dayStart == todayTimestamp

            val status = if (dayStart < firstRecordTime) {
                ComplianceDayStatus.BLANK
            } else {
                val completedForDay = workoutsWithSets.count {
                    it.workout.date in dayStart..dayEnd && it.workout.status == "COMPLETED"
                }
                val isScheduled = dayCode in listOf("L", "M", "X", "V")
                val scheduledCount = if (isScheduled) 1 else 0

                when {
                    isToday && isScheduled && completedForDay < scheduledCount -> ComplianceDayStatus.NEUTRAL_TODAY
                    isScheduled && completedForDay >= scheduledCount -> ComplianceDayStatus.GREEN
                    isScheduled && completedForDay < scheduledCount -> ComplianceDayStatus.RED
                    else -> ComplianceDayStatus.GRAY
                }
            }

            val isScheduled = dayCode in listOf("L", "M", "X", "V")
            val scheduledCount = if (isScheduled) 1 else 0
            val completedForDay = workoutsWithSets.count {
                it.workout.date in dayStart..dayEnd && it.workout.status == "COMPLETED"
            }

            result.add(
                DayComplianceInfo(
                    dateString = sdf.format(Date(cal.timeInMillis)),
                    timestamp = cal.timeInMillis,
                    status = status,
                    scheduledCount = if (status == ComplianceDayStatus.BLANK) 0 else scheduledCount,
                    completedCount = if (status == ComplianceDayStatus.BLANK) 0 else completedForDay,
                    isToday = isToday
                )
            )

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        result
    }

    // Streaks Calculation (only from first record onwards)
    val currentStreak = remember(daysList) {
        var streak = 0
        val reverseSorted = daysList.filter { it.status != ComplianceDayStatus.BLANK }.sortedByDescending { it.timestamp }
        for (day in reverseSorted) {
            if (day.status == ComplianceDayStatus.GREEN) {
                streak++
            } else if (day.status == ComplianceDayStatus.RED) {
                break
            }
        }
        streak
    }

    val bestStreak = remember(daysList) {
        var maxS = 0
        var currentS = 0
        for (day in daysList.filter { it.status != ComplianceDayStatus.BLANK }) {
            if (day.status == ComplianceDayStatus.GREEN) {
                currentS++
                if (currentS > maxS) maxS = currentS
            } else if (day.status == ComplianceDayStatus.RED) {
                currentS = 0
            }
        }
        maxS
    }

    val completionPercentage = remember(daysList) {
        val activeDays = daysList.filter { it.status != ComplianceDayStatus.BLANK }
        val totalScheduled = activeDays.count { it.scheduledCount > 0 }
        val totalCompleted = activeDays.count { it.status == ComplianceDayStatus.GREEN }
        if (totalScheduled > 0) (totalCompleted.toFloat() / totalScheduled.toFloat() * 100).toInt() else 100
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Constancia (GitHub Style)", fontWeight = FontWeight.Bold) },
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
            // Streaks Cards Row (Duolingo Style)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Streak
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = DuolingoYellow)
                                Text("Racha Actual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$currentStreak días", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DuolingoYellow)
                        }
                    }

                    // Best Streak
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = DuolingoGreen)
                                Text("Mejor Racha", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$bestStreak días", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DuolingoGreen)
                        }
                    }
                }
            }

            // Compliance Rate Banner Card
            item {
                DuolingoCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tasa de Cumplimiento", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$completionPercentage%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = DuolingoGreen)
                        }
                        DuolingoProgressBar(
                            progress = completionPercentage / 100f,
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }

            // GitHub Contribution Style Grid Matrix Card
            item {
                DuolingoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Calendario de Constancia (Desde tu Registro)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                        // Legend Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(DuolingoGreen))
                                Text("Cumplido", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(DuolingoRed))
                                Text("Pendiente", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE5E7EB)))
                                Text("Descanso", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Grid Matrix
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(10),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            items(daysList) { day ->
                                val boxColor = when (day.status) {
                                    ComplianceDayStatus.GREEN -> DuolingoGreen
                                    ComplianceDayStatus.RED -> DuolingoRed
                                    ComplianceDayStatus.GRAY -> Color(0xFFE5E7EB)
                                    ComplianceDayStatus.NEUTRAL_TODAY -> MaterialTheme.colorScheme.surface
                                    ComplianceDayStatus.BLANK -> Color.Transparent
                                }

                                val borderModifier = when {
                                    day.status == ComplianceDayStatus.BLANK -> Modifier
                                    day.isToday -> Modifier.border(2.dp, DuolingoGreen, RoundedCornerShape(6.dp))
                                    else -> Modifier
                                }

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(boxColor)
                                        .then(borderModifier)
                                        .clickable(enabled = day.status != ComplianceDayStatus.BLANK) {
                                            haptics.performClick()
                                            selectedDayInfo = day
                                            showDayDetailDialog = true
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Day Details Dialog
    if (showDayDetailDialog && selectedDayInfo != null) {
        val d = selectedDayInfo!!
        AlertDialog(
            onDismissRequest = { showDayDetailDialog = false },
            title = { Text("Detalle del Día ${d.dateString}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (d.status) {
                            ComplianceDayStatus.GREEN -> "🟢 ¡Rutina completada con éxito!"
                            ComplianceDayStatus.RED -> "🔴 Día con rutina programada no completada."
                            ComplianceDayStatus.GRAY -> "⚪ Día de descanso sin rutina programada."
                            ComplianceDayStatus.NEUTRAL_TODAY -> "🔳 Día de hoy en curso (pendiente)."
                            ComplianceDayStatus.BLANK -> ""
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("• Sesiones completadas: ${d.completedCount}", fontSize = 13.sp)
                    Text("• Rutinas programadas: ${d.scheduledCount}", fontSize = 13.sp)
                }
            },
            confirmButton = {
                DuolingoButton(
                    text = "Cerrar",
                    onClick = { showDayDetailDialog = false },
                    modifier = Modifier.width(110.dp)
                )
            }
        )
    }
}
