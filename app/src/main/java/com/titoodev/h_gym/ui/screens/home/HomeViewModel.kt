package com.titoodev.h_gym.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.FoodLogEntity
import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.entities.WorkoutWithSets
import com.titoodev.h_gym.data.repository.HGymRepository
import com.titoodev.h_gym.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HGymDatabase.getDatabase(application)
    private val repository = HGymRepository(
        db.userDao(),
        db.bodyMeasurementDao(),
        db.exerciseDao(),
        db.workoutDao(),
        db.foodDao(),
        db.quotaDao()
    )

    val selectedDateTimestamp = MutableStateFlow(System.currentTimeMillis())

    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestMeasurementState: StateFlow<BodyMeasurementEntity?> = repository.latestMeasurementFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodLogsForSelectedDate: StateFlow<List<FoodLogEntity>> = selectedDateTimestamp.flatMapLatest { timestamp ->
        repository.getFoodLogsForDateRange(
            DateUtils.getStartOfDay(timestamp),
            DateUtils.getEndOfDay(timestamp)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val workoutsForSelectedDate: StateFlow<List<WorkoutWithSets>> = selectedDateTimestamp.flatMapLatest { timestamp ->
        repository.getWorkoutsForDateRange(
            DateUtils.getStartOfDay(timestamp),
            DateUtils.getEndOfDay(timestamp)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSelectedDate(timestamp: Long) {
        selectedDateTimestamp.value = timestamp
    }
}
