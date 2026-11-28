package com.titoodev.h_gym.ui.screens.exercises

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.HGymApplication
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.util.ImageUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseCatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HGymApplication).container.repository

    val exercisesState: StateFlow<List<ExerciseEntity>> = repository.exercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveExercise(
        id: Long = 0L,
        name: String,
        description: String?,
        photoUri: Uri?,
        muscleGroup: String = "Pecho",
        secondaryMuscles: String? = null,
        equipment: String? = "Barra",
        exerciseType: String = "Fuerza",
        personalNotes: String? = null
    ) {
        viewModelScope.launch {
            val existing = if (id > 0L) repository.getExerciseById(id) else null
            var compressedPath: String? = existing?.localPhotoPath

            if (photoUri != null) {
                val compressedFile = ImageUtils.compressImageTo100Kb(getApplication(), photoUri)
                compressedPath = compressedFile?.absolutePath ?: existing?.localPhotoPath
            }

            val exercise = ExerciseEntity(
                id = id,
                name = name,
                muscleGroup = muscleGroup,
                secondaryMuscles = secondaryMuscles,
                description = description,
                localPhotoPath = compressedPath,
                equipment = equipment,
                exerciseType = exerciseType,
                personalNotes = personalNotes,
                isCustom = true,
                updatedAt = System.currentTimeMillis()
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
