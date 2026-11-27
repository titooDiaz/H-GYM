package com.titoodev.h_gym.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profile_history")
data class UserProfileHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val uid: String,
    val fieldName: String,
    val previousValue: String?,
    val newValue: String,
    val timestamp: Long = System.currentTimeMillis()
)
