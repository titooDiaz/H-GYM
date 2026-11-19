package com.titoodev.h_gym.ui.screens.exercises

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.repository.HGymRepository
import com.titoodev.h_gym.util.ImageUtils
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

    fun saveExercise(
        id: Long = 0L,
        name: String,
        description: String?,
        photoUri: Uri?,
        muscleGroup: String = "General",
        equipment: String? = "Libre"
    ) {
        viewModelScope.launch {
            var compressedPath: String? = null
            if (photoUri != null) {
                val compressedFile = ImageUtils.compressImageTo100Kb(getApplication(), photoUri)
                compressedPath = compressedFile?.absolutePath
            }

            val exercise = ExerciseEntity(
                id = id,
                name = name,
                muscleGroup = muscleGroup,
                description = description,
                localPhotoPath = compressedPath,
                equipment = equipment,
                exerciseType = "Fuerza",
                isCustom = true
            )
            repository.addExercise(exercise)
        }
    }

    suspend fun getRoutinesCountForExercise(exerciseId: Long): Int {
        return repository.getRoutinesCountForExercise(exerciseId)
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
        }
    }
}
