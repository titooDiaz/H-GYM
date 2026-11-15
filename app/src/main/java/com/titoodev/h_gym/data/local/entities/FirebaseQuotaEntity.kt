package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "firebase_quota_metrics")
data class FirebaseQuotaEntity(
    @PrimaryKey val id: Int = 1,
    val estimatedReadsToday: Int = 0,
    val estimatedWritesToday: Int = 0,
    val estimatedDeletesToday: Int = 0,
    val estimatedEgressBytesMonth: Long = 0L,
    val estimatedStorageBytes: Long = 0L,
    val lastResetPstTimestamp: Long = 0L,
    val isWriteQuotaPaused: Boolean = false,
    val isReadQuotaPaused: Boolean = false
)
