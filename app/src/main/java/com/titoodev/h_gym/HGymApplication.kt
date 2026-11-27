package com.titoodev.h_gym

import android.app.Application
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.repository.HGymRepository
import com.titoodev.h_gym.domain.repository.*
import com.titoodev.h_gym.domain.usecase.*

class AppContainer(private val application: Application) {
    val database: HGymDatabase by lazy {
        HGymDatabase.getDatabase(application)
    }

    val repository: HGymRepository by lazy {
        HGymRepository(
            database.userDao(),
            database.userProfileHistoryDao(),
            database.bodyMeasurementDao(),
            database.exerciseDao(),
            database.workoutDao(),
            database.foodDao(),
            database.quotaDao(),
            database.syncOutboxDao()
        )
    }

    // Domain Repositories
    val userRepository: IUserRepository get() = repository
    val workoutRepository: IWorkoutRepository get() = repository
    val exerciseRepository: IExerciseRepository get() = repository
    val nutritionRepository: INutritionRepository get() = repository
    val bodyMeasurementRepository: IBodyMeasurementRepository get() = repository

    // Use Cases
    val calculate1RMUseCase by lazy { Calculate1RMUseCase() }
    val calculateVolumeUseCase by lazy { CalculateVolumeUseCase() }
    val detectPersonalRecordsUseCase by lazy { DetectPersonalRecordsUseCase(calculate1RMUseCase) }
    val calculateChallengeProgressUseCase by lazy { CalculateChallengeProgressUseCase() }
}

class HGymApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
