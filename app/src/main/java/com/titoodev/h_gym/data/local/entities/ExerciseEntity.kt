package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val secondaryMuscles: String? = null,
    val description: String? = null,
    val localPhotoPath: String? = null,
    val equipment: String? = null,
    val exerciseType: String = "Fuerza",
    val personalNotes: String? = null,
    val isCustom: Boolean = false
)
