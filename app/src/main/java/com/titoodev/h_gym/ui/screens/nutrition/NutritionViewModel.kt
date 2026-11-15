package com.titoodev.h_gym.ui.screens.nutrition

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.FoodLogEntity
import com.titoodev.h_gym.data.repository.HGymRepository
import com.titoodev.h_gym.util.DateUtils
import com.titoodev.h_gym.util.ImageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NutritionViewModel(application: Application) : AndroidViewModel(application) {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodLogsState: StateFlow<List<FoodLogEntity>> = selectedDateTimestamp.flatMapLatest { timestamp ->
        repository.getFoodLogsForDateRange(
            DateUtils.getStartOfDay(timestamp),
            DateUtils.getEndOfDay(timestamp)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodLogsState: StateFlow<List<FoodLogEntity>> = repository.foodLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(timestamp: Long) {
        selectedDateTimestamp.value = timestamp
    }

    fun saveMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        description: String?,
        photoUri: Uri?,
        timestamp: Long = selectedDateTimestamp.value
    ) {
        viewModelScope.launch {
            var compressedPath: String? = null
            if (photoUri != null) {
                val compressedFile = ImageUtils.compressImageTo100Kb(getApplication(), photoUri)
                compressedPath = compressedFile?.absolutePath
            }

            val foodLog = FoodLogEntity(
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                description = description,
                localPhotoPath = compressedPath,
                timestamp = timestamp
            )
            repository.addFoodLog(foodLog)
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
        }
    }
}
