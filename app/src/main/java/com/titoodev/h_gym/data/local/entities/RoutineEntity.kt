package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetType: String = "Fuerza",
    val daysOfWeek: String = "L,M,X,J,V" // Comma-separated (L, M, X, J, V, S, D)
)
