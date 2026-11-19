package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,
    val durationSeconds: Long,
    val notes: String? = null,
    val routineId: Long? = null,
    val status: String = "COMPLETED", // "COMPLETED" or "INCOMPLETE"
    val actualExerciseOrderJson: String? = null
)
