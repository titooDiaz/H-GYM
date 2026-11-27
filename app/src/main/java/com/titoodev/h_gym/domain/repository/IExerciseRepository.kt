package com.titoodev.h_gym.domain.repository

import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {
    val exercisesFlow: Flow<List<ExerciseEntity>>
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    suspend fun addExercise(exercise: ExerciseEntity): Long
    suspend fun deleteExercise(exerciseId: Long)
    suspend fun getRoutinesCountForExercise(exerciseId: Long): Int
    fun getSetsHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>
}
