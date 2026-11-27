package com.titoodev.h_gym.domain.repository

import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.entities.UserProfileHistoryEntity
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    val userFlow: Flow<UserEntity?>
    val profileHistoryFlow: Flow<List<UserProfileHistoryEntity>>
    suspend fun saveUser(user: UserEntity)
    suspend fun recordProfileHistoryChange(history: UserProfileHistoryEntity)
    suspend fun clearUser()
}
