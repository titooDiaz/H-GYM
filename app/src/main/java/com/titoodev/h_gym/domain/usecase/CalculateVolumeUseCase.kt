package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity

class CalculateVolumeUseCase {
    operator fun invoke(sets: List<WorkoutSetEntity>): Float {
        return sets.fold(0f) { acc, set ->
            if (set.weight > 0f && set.reps > 0) {
                acc + (set.weight * set.reps)
            } else acc
        }
    }
}
