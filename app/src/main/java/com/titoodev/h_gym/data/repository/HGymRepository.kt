package com.titoodev.h_gym.data.repository

import com.titoodev.h_gym.data.local.dao.*
import com.titoodev.h_gym.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class HGymRepository(
    private val userDao: UserDao,
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val foodDao: FoodDao,
    private val quotaDao: QuotaDao
) {
    // User & Profile
    val userFlow: Flow<UserEntity?> = userDao.getUser()
    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun clearUser() = userDao.clearUsers()

    // Body Measurements
    val measurementsFlow: Flow<List<BodyMeasurementEntity>> = bodyMeasurementDao.getAllMeasurements()
    val latestMeasurementFlow: Flow<BodyMeasurementEntity?> = bodyMeasurementDao.getLatestMeasurement()
    fun getMeasurementsForRange(startTimestamp: Long, endTimestamp: Long): Flow<List<BodyMeasurementEntity>> =
        bodyMeasurementDao.getMeasurementsForRange(startTimestamp, endTimestamp)
    suspend fun addMeasurement(measurement: BodyMeasurementEntity) = bodyMeasurementDao.insertMeasurement(measurement)

    // Exercises
    val exercisesFlow: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> = exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    suspend fun getExerciseById(id: Long): ExerciseEntity? = exerciseDao.getExerciseById(id)
    suspend fun addExercise(exercise: ExerciseEntity): Long = exerciseDao.insertExercise(exercise)
    fun getExerciseHistory(exerciseId: Long): Flow<List<ExercisePerformanceEntity>> = exerciseDao.getPerformanceHistory(exerciseId)
    fun getSetsHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> = exerciseDao.getSetsHistoryForExercise(exerciseId)
    suspend fun addExercisePerformance(performance: ExercisePerformanceEntity) = exerciseDao.insertPerformance(performance)

    // Workouts & Routines
    val workoutsFlow: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    val workoutsWithSetsFlow: Flow<List<WorkoutWithSets>> = workoutDao.getAllWorkoutsWithSets()
    fun getWorkoutsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WorkoutWithSets>> =
        workoutDao.getWorkoutsForDateRange(startTimestamp, endTimestamp)
    suspend fun saveWorkout(workout: WorkoutEntity): Long = workoutDao.insertWorkout(workout)
    suspend fun saveWorkoutSet(set: WorkoutSetEntity) = workoutDao.insertWorkoutSet(set)
    val routinesFlow: Flow<List<RoutineEntity>> = workoutDao.getAllRoutines()
    val routinesWithItemsFlow: Flow<List<RoutineWithItems>> = workoutDao.getAllRoutinesWithItems()
    suspend fun saveRoutine(routine: RoutineEntity): Long = workoutDao.insertRoutine(routine)
    suspend fun saveRoutineItem(item: RoutineItemEntity) = workoutDao.insertRoutineItem(item)

    // Food & Nutrition
    val foodLogsFlow: Flow<List<FoodLogEntity>> = foodDao.getAllFoodLogs()
    fun getFoodLogsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<FoodLogEntity>> =
        foodDao.getFoodLogsForDateRange(startTimestamp, endTimestamp)
    suspend fun addFoodLog(foodLog: FoodLogEntity) = foodDao.insertFoodLog(foodLog)
    suspend fun deleteFoodLog(id: Long) = foodDao.deleteFoodLog(id)

    // Quotas
    val quotaMetricsFlow: Flow<FirebaseQuotaEntity?> = quotaDao.getQuotaMetrics()
    suspend fun updateQuotaMetrics(metrics: FirebaseQuotaEntity) = quotaDao.updateQuotaMetrics(metrics)
}
