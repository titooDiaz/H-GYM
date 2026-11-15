package com.titoodev.h_gym.ui.screens.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.*
import com.titoodev.h_gym.data.repository.HGymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HGymDatabase.getDatabase(application)
    private val repository = HGymRepository(
        db.userDao(),
        db.bodyMeasurementDao(),
        db.exerciseDao(),
        db.workoutDao(),
        db.foodDao(),
        db.quotaDao()
    )

    val workoutsWithSetsState: StateFlow<List<WorkoutWithSets>> = repository.workoutsWithSetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routinesWithItemsState: StateFlow<List<RoutineWithItems>> = repository.routinesWithItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercisesState: StateFlow<List<ExerciseEntity>> = repository.exercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveWorkoutSession(
        title: String,
        startTime: Long,
        durationSeconds: Long,
        notes: String?,
        sets: List<WorkoutSetEntity>
    ) {
        viewModelScope.launch {
            val workout = WorkoutEntity(
                title = title,
                date = startTime,
                durationSeconds = durationSeconds,
                notes = notes
            )
            val workoutId = repository.saveWorkout(workout)
            sets.forEach { setItem ->
                repository.saveWorkoutSet(setItem.copy(workoutId = workoutId))
            }
        }
    }

    fun saveRoutine(name: String, description: String, targetType: String) {
        viewModelScope.launch {
            val routine = RoutineEntity(
                name = name,
                description = description,
                targetType = targetType
            )
            repository.saveRoutine(routine)
        }
    }
}
