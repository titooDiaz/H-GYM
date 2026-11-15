package com.titoodev.h_gym.data.sync

import com.titoodev.h_gym.data.local.dao.QuotaDao
import com.titoodev.h_gym.data.local.entities.FirebaseQuotaEntity
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

object QuotaManager {
    // Official Spark Free Tier Limits
    const val MAX_SPARK_WRITES_DAILY = 20_000
    const val MAX_SPARK_READS_DAILY = 50_000
    const val MAX_SPARK_DELETES_DAILY = 20_000
    const val MAX_SPARK_EGRESS_MONTHLY_BYTES = 10L * 1024 * 1024 * 1024 // 10 GB

    // Conservative Preventive Thresholds (75% to prevent overshoots)
    const val PREVENTIVE_WRITES_THRESHOLD = (MAX_SPARK_WRITES_DAILY * 0.75).toInt() // 15,000
    const val PREVENTIVE_READS_THRESHOLD = (MAX_SPARK_READS_DAILY * 0.75).toInt() // 37,500
    const val PREVENTIVE_DELETES_THRESHOLD = (MAX_SPARK_DELETES_DAILY * 0.75).toInt() // 15,000

    fun isWriteSyncAllowed(metrics: FirebaseQuotaEntity?): Boolean {
        if (metrics == null) return true
        if (metrics.estimatedWritesToday >= PREVENTIVE_WRITES_THRESHOLD) return false
        if (metrics.estimatedDeletesToday >= PREVENTIVE_DELETES_THRESHOLD) return false
        return !metrics.isWriteQuotaPaused
    }

    fun isReadSyncAllowed(metrics: FirebaseQuotaEntity?): Boolean {
        if (metrics == null) return true
        if (metrics.estimatedReadsToday >= PREVENTIVE_READS_THRESHOLD) return false
        return !metrics.isReadQuotaPaused
    }

    suspend fun recordOperation(quotaDao: QuotaDao, reads: Int = 0, writes: Int = 0, deletes: Int = 0) {
        val current = quotaDao.getQuotaMetrics().firstOrNull() ?: FirebaseQuotaEntity()
        val pstCalendar = Calendar.getInstance(TimeZone.getTimeZone("PST"))
        val nowPst = pstCalendar.timeInMillis

        // Reset if PST day changed
        val lastResetCal = Calendar.getInstance(TimeZone.getTimeZone("PST")).apply {
            timeInMillis = current.lastResetPstTimestamp
        }

        val isSameDay = pstCalendar.get(Calendar.YEAR) == lastResetCal.get(Calendar.YEAR) &&
                pstCalendar.get(Calendar.DAY_OF_YEAR) == lastResetCal.get(Calendar.DAY_OF_YEAR)

        val updated = if (isSameDay) {
            current.copy(
                estimatedReadsToday = current.estimatedReadsToday + reads,
                estimatedWritesToday = current.estimatedWritesToday + writes,
                estimatedDeletesToday = current.estimatedDeletesToday + deletes
            )
        } else {
            FirebaseQuotaEntity(
                estimatedReadsToday = reads,
                estimatedWritesToday = writes,
                estimatedDeletesToday = deletes,
                lastResetPstTimestamp = nowPst
            )
        }

        quotaDao.updateQuotaMetrics(updated)
    }
}
