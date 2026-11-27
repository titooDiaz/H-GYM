package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val birthDateTimestamp: Long? = null,
    val sex: String? = null,
    val height: Float? = null,
    val currentWeight: Float? = null,
    val goals: String = "",
    val experienceLevel: String = "Intermedio",
    val trainingFrequency: Int = 4,
    val preferredTrainingType: String = "Fuerza",
    val preferredUnit: String = "kg",
    val syncId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val deletedAt: Long? = null,
    val syncState: String = "SYNCED"
)
