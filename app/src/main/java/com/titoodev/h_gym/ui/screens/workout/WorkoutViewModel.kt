package com.titoodev.h_gym.ui.screens.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.HGymApplication
import com.titoodev.h_gym.data.local.entities.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HGymApplication).container.repository

    val workoutsWithSetsState: StateFlow<List<WorkoutWithSets>> = repository.workoutsWithSetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routinesWithItemsState: StateFlow<List<RoutineWithItems>> = repository.routinesWithItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercisesState: StateFlow<List<ExerciseEntity>> = repository.exercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun checkIncompleteWorkout(onFound: (WorkoutEntity) -> Unit) {
        viewModelScope.launch {
            val incomplete = repository.getIncompleteWorkout()
            if (incomplete != null) {
                onFound(incomplete)
            }
        }
    }

    fun saveWorkoutSession(
        title: String,
        startTime: Long,
        durationSeconds: Long,
        notes: String?,
        sets: List<WorkoutSetEntity>,
        routineId: Long? = null,
        status: String = "COMPLETED"
    ) {
        viewModelScope.launch {
            val workout = WorkoutEntity(
                title = title,
                date = startTime,
                durationSeconds = durationSeconds,
                notes = notes,
                routineId = routineId,
                status = status
            )
            val workoutId = repository.saveWorkout(workout)
            sets.forEach { setItem ->
                repository.saveWorkoutSet(setItem.copy(workoutId = workoutId))
            }
            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
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
            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
        }
    }

    fun saveFullRoutine(
        id: Long = 0L,
        name: String,
        daysOfWeek: String,
        exerciseIds: List<Long>
    ) {
        viewModelScope.launch {
            if (id > 0L) {
                repository.deleteRoutine(id)
            }
            val routine = RoutineEntity(
                id = if (id > 0L) id else 0L,
                name = name,
                description = "Rutina personalizada (${daysOfWeek})",
                targetType = "Semanal",
                daysOfWeek = daysOfWeek
            )
            val routineId = repository.saveRoutine(routine)
            exerciseIds.forEachIndexed { idx, exId ->
                repository.saveRoutineItem(
                    RoutineItemEntity(
                        routineId = routineId,
                        exerciseId = exId,
                        orderIndex = idx
                    )
                )
            }
            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
        }
    }
}
