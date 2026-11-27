package com.titoodev.h_gym.domain.repository

import com.titoodev.h_gym.data.local.entities.*
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    val workoutsFlow: Flow<List<WorkoutEntity>>
    val workoutsWithSetsFlow: Flow<List<WorkoutWithSets>>
    val routinesFlow: Flow<List<RoutineEntity>>
    val routinesWithItemsFlow: Flow<List<RoutineWithItems>>

    fun getWorkoutsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WorkoutWithSets>>
    suspend fun saveWorkout(workout: WorkoutEntity): Long
    suspend fun saveWorkoutSet(set: WorkoutSetEntity)
    suspend fun saveRoutine(routine: RoutineEntity): Long
    suspend fun saveRoutineItem(item: RoutineItemEntity)
    suspend fun deleteRoutine(routineId: Long)
    suspend fun getIncompleteWorkout(): WorkoutEntity?
}
