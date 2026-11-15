package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String,
    val foodName: String,
    val calories: Int,
    val description: String?,
    val localPhotoPath: String?,
    val timestamp: Long
)
