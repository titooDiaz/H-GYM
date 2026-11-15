package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val weight: Float,
    val chest: Float?,
    val waist: Float?,
    val hips: Float?,
    val arm: Float?,
    val thigh: Float?,
    val bodyFatPercentage: Float?,
    val timestamp: Long
)
