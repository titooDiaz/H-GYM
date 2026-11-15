package com.titoodev.h_gym.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.repository.HGymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseCatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HGymDatabase.getDatabase(application)
    private val repository = HGymRepository(
        db.userDao(),
        db.bodyMeasurementDao(),
        db.exerciseDao(),
        db.workoutDao(),
        db.foodDao(),
        db.quotaDao()
    )

    val exercisesState: StateFlow<List<ExerciseEntity>> = repository.exercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String?,
        exerciseType: String = "Fuerza"
    ) {
        viewModelScope.launch {
            repository.addExercise(
                ExerciseEntity(
                    name = name,
                    muscleGroup = muscleGroup,
                    equipment = equipment,
                    exerciseType = exerciseType,
                    isCustom = true
                )
            )
        }
    }
}
