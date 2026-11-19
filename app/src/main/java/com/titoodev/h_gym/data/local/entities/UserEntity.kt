package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val age: Int?,
    val sex: String?,
    val height: Float?,
    val currentWeight: Float?,
    val goals: String, // Comma separated goals
    val experienceLevel: String,
    val trainingFrequency: Int,
    val preferredTrainingType: String,
    val preferredUnit: String = "kg",
    val updatedAt: Long
)
