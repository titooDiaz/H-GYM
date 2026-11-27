package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val weight: Float,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val arm: Float? = null,
    val forearm: Float? = null,
    val thigh: Float? = null,
    val calf: Float? = null,
    val bodyFatPercentage: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val syncId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val deletedAt: Long? = null,
    val syncState: String = "SYNCED"
)
