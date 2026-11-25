package com.titoodev.h_gym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.FirebaseQuotaEntity
import com.titoodev.h_gym.data.sync.QuotaManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotaAdminScreen() {
    val context = LocalContext.current
    val db = remember { HGymDatabase.getDatabase(context) }
    val quotaMetricsState by db.quotaDao().getQuotaMetrics().collectAsState(initial = FirebaseQuotaEntity())
    val metrics = quotaMetricsState ?: FirebaseQuotaEntity()

    val writesProgress = (metrics.estimatedWritesToday.toFloat() / QuotaManager.PREVENTIVE_WRITES_THRESHOLD).coerceIn(0f, 1f)
    val readsProgress = (metrics.estimatedReadsToday.toFloat() / QuotaManager.PREVENTIVE_READS_THRESHOLD).coerceIn(0f, 1f)
    val deletesProgress = (metrics.estimatedDeletesToday.toFloat() / QuotaManager.PREVENTIVE_DELETES_THRESHOLD).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Métricas Reales Registradas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monitor preventivo activo: si el uso alcanza el 75% de los límites del plan gratuito Spark, la app detiene la sincronización en la nube y opera 100% offline en almacenamiento local.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Escrituras Diarias Estimadas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { writesProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${metrics.estimatedWritesToday} / ${QuotaManager.PREVENTIVE_WRITES_THRESHOLD} (Umbral 75% Spark)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Lecturas Diarias Estimadas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { readsProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${metrics.estimatedReadsToday} / ${QuotaManager.PREVENTIVE_READS_THRESHOLD} (Umbral 75% Spark)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Eliminaciones Diarias Estimadas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { deletesProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${metrics.estimatedDeletesToday} / ${QuotaManager.PREVENTIVE_DELETES_THRESHOLD} (Umbral 75% Spark)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
}
