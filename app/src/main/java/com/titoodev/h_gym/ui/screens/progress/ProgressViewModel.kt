package com.titoodev.h_gym.ui.screens.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.HGymApplication
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.WorkoutWithSets
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Exercise1RMInfo(
    val exerciseName: String,
    val muscleGroup: String,
    val maxWeight: Float,
    val maxReps: Int,
    val estimated1RM: Float
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HGymApplication).container.repository

    val allMeasurementsState: StateFlow<List<BodyMeasurementEntity>> = repository.measurementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurementState: StateFlow<BodyMeasurementEntity?> = repository.latestMeasurementFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutsWithSetsState: StateFlow<List<WorkoutWithSets>> = repository.workoutsWithSetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercisesState: StateFlow<List<ExerciseEntity>> = repository.exercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real calculated 1RM for all recorded exercises
    val exercise1RMListState: StateFlow<List<Exercise1RMInfo>> = combine(
        workoutsWithSetsState,
        allExercisesState
    ) { workouts, exercises ->
        val exerciseMap = exercises.associateBy { it.id }
        val setsByExercise = workouts.flatMap { it.sets }.groupBy { it.exerciseId }

        setsByExercise.mapNotNull { (exerciseId, sets) ->
            val exercise = exerciseMap[exerciseId] ?: return@mapNotNull null
            val bestSet = sets.maxByOrNull { setItem ->
                if (setItem.reps == 1) setItem.weight
                else setItem.weight * (1f + setItem.reps / 30f)
            } ?: return@mapNotNull null

            val e1rm = if (bestSet.reps == 1) bestSet.weight else bestSet.weight * (1f + bestSet.reps / 30f)

            Exercise1RMInfo(
                exerciseName = exercise.name,
                muscleGroup = exercise.muscleGroup,
                maxWeight = bestSet.weight,
                maxReps = bestSet.reps,
                estimated1RM = e1rm
            )
        }.sortedByDescending { it.estimated1RM }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMeasurement(
        weight: Float,
        chest: Float?,
        waist: Float?,
        hips: Float?,
        arm: Float?,
        forearm: Float? = null,
        thigh: Float?,
        calf: Float? = null,
        bodyFatPercentage: Float?
    ) {
        viewModelScope.launch {
            val user = repository.userFlow.firstOrNull()
            val measurement = BodyMeasurementEntity(
                uid = user?.uid ?: "local_guest",
                weight = weight,
                chest = chest,
                waist = waist,
                hips = hips,
                arm = arm,
                forearm = forearm,
                thigh = thigh,
                calf = calf,
                bodyFatPercentage = bodyFatPercentage,
                timestamp = System.currentTimeMillis()
            )
            repository.addMeasurement(measurement)
            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
        }
    }
}
