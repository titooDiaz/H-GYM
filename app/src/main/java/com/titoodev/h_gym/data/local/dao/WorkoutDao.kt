package com.titoodev.h_gym.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.titoodev.h_gym.data.local.entities.RoutineEntity
import com.titoodev.h_gym.data.local.entities.RoutineItemEntity
import com.titoodev.h_gym.data.local.entities.RoutineWithItems
import com.titoodev.h_gym.data.local.entities.WorkoutEntity
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import com.titoodev.h_gym.data.local.entities.WorkoutWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkoutsWithSets(): Flow<List<WorkoutWithSets>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startTimestamp AND :endTimestamp ORDER BY date DESC")
    fun getWorkoutsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WorkoutWithSets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM routines ORDER BY name ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Transaction
    @Query("SELECT * FROM routines ORDER BY name ASC")
    fun getAllRoutinesWithItems(): Flow<List<RoutineWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItem(item: RoutineItemEntity)
}
