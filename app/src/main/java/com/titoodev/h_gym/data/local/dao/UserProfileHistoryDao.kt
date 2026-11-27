package com.titoodev.h_gym.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titoodev.h_gym.data.local.entities.UserProfileHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileHistoryDao {
    @Query("SELECT * FROM user_profile_history WHERE uid = :uid ORDER BY timestamp DESC")
    fun getProfileHistory(uid: String): Flow<List<UserProfileHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileHistory(history: UserProfileHistoryEntity)
}
