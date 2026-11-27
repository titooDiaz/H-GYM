package com.titoodev.h_gym.domain.repository

import com.titoodev.h_gym.data.local.entities.FoodLogEntity
import kotlinx.coroutines.flow.Flow

interface INutritionRepository {
    val foodLogsFlow: Flow<List<FoodLogEntity>>
    fun getFoodLogsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<FoodLogEntity>>
    suspend fun addFoodLog(foodLog: FoodLogEntity)
    suspend fun deleteFoodLog(id: Long)
}
