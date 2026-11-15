package com.titoodev.h_gym.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titoodev.h_gym.data.local.entities.FirebaseQuotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotaDao {
    @Query("SELECT * FROM firebase_quota_metrics WHERE id = 1 LIMIT 1")
    fun getQuotaMetrics(): Flow<FirebaseQuotaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateQuotaMetrics(metrics: FirebaseQuotaEntity)
}
