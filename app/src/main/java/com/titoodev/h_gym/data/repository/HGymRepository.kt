package com.titoodev.h_gym.data.repository

import com.titoodev.h_gym.data.local.dao.*
import com.titoodev.h_gym.data.local.entities.*
import com.titoodev.h_gym.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class HGymRepository(
    private val userDao: UserDao,
    private val userProfileHistoryDao: UserProfileHistoryDao,
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val foodDao: FoodDao,
    private val quotaDao: QuotaDao,
    private val syncOutboxDao: SyncOutboxDao
) : IUserRepository, IWorkoutRepository, IExerciseRepository, INutritionRepository, IBodyMeasurementRepository {

    // User & Profile
    override val userFlow: Flow<UserEntity?> = userDao.getUser()
    override val profileHistoryFlow: Flow<List<UserProfileHistoryEntity>> = flowOf(emptyList())
    override suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)
    override suspend fun recordProfileHistoryChange(history: UserProfileHistoryEntity) {
        userProfileHistoryDao.insertProfileHistory(history)
    }
    override suspend fun clearUser() = userDao.clearUsers()

    // Body Measurements
    override val measurementsFlow: Flow<List<BodyMeasurementEntity>> = bodyMeasurementDao.getAllMeasurements()
    override val latestMeasurementFlow: Flow<BodyMeasurementEntity?> = bodyMeasurementDao.getLatestMeasurement()
    override fun getMeasurementsForRange(startTimestamp: Long, endTimestamp: Long): Flow<List<BodyMeasurementEntity>> =
        bodyMeasurementDao.getMeasurementsForRange(startTimestamp, endTimestamp)
    override suspend fun addMeasurement(measurement: BodyMeasurementEntity) = bodyMeasurementDao.insertMeasurement(measurement)

    // Exercises
    override val exercisesFlow: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> = exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    override suspend fun getExerciseById(id: Long): ExerciseEntity? = exerciseDao.getExerciseById(id)
    override suspend fun addExercise(exercise: ExerciseEntity): Long = exerciseDao.insertExercise(exercise)
    override suspend fun deleteExercise(exerciseId: Long) = exerciseDao.deleteExercise(exerciseId)
    override suspend fun getRoutinesCountForExercise(exerciseId: Long): Int = exerciseDao.getRoutinesCountForExercise(exerciseId)
    override fun getSetsHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> = exerciseDao.getSetsHistoryForExercise(exerciseId)

    // Workouts & Routines
    override val workoutsFlow: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    override val workoutsWithSetsFlow: Flow<List<WorkoutWithSets>> = workoutDao.getAllWorkoutsWithSets()
    override fun getWorkoutsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WorkoutWithSets>> =
        workoutDao.getWorkoutsForDateRange(startTimestamp, endTimestamp)
    override suspend fun saveWorkout(workout: WorkoutEntity): Long = workoutDao.insertWorkout(workout)
    override suspend fun saveWorkoutSet(set: WorkoutSetEntity) = workoutDao.insertWorkoutSet(set)
    override val routinesFlow: Flow<List<RoutineEntity>> = workoutDao.getAllRoutines()
    override val routinesWithItemsFlow: Flow<List<RoutineWithItems>> = workoutDao.getAllRoutinesWithItems()
    override suspend fun saveRoutine(routine: RoutineEntity): Long = workoutDao.insertRoutine(routine)
    override suspend fun saveRoutineItem(item: RoutineItemEntity) = workoutDao.insertRoutineItem(item)
    override suspend fun deleteRoutine(routineId: Long) {
        workoutDao.deleteRoutineItems(routineId)
        workoutDao.deleteRoutine(routineId)
    }
    override suspend fun getIncompleteWorkout(): WorkoutEntity? = workoutDao.getIncompleteWorkout()

    // Food & Nutrition
    override val foodLogsFlow: Flow<List<FoodLogEntity>> = foodDao.getAllFoodLogs()
    override fun getFoodLogsForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<FoodLogEntity>> =
        foodDao.getFoodLogsForDateRange(startTimestamp, endTimestamp)
    override suspend fun addFoodLog(foodLog: FoodLogEntity) = foodDao.insertFoodLog(foodLog)
    override suspend fun deleteFoodLog(id: Long) = foodDao.deleteFoodLog(id)

    // Quotas & Outbox
    val quotaMetricsFlow: Flow<FirebaseQuotaEntity?> = quotaDao.getQuotaMetrics()
    suspend fun updateQuotaMetrics(metrics: FirebaseQuotaEntity) = quotaDao.updateQuotaMetrics(metrics)
    val pendingOutboxFlow: Flow<List<SyncOutboxEntity>> = syncOutboxDao.getPendingOutboxItems()
    suspend fun addOutboxItem(item: SyncOutboxEntity) = syncOutboxDao.insertOutboxItem(item)
    suspend fun deleteOutboxItem(id: String) = syncOutboxDao.deleteOutboxItem(id)
}
