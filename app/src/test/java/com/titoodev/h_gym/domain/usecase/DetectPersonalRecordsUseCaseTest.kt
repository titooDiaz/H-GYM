package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectPersonalRecordsUseCaseTest {
    private val useCase = DetectPersonalRecordsUseCase()

    @Test
    fun `detect new personal record when weight exceeds historical`() {
        val historical = listOf(
            WorkoutSetEntity(id = 1, workoutId = 1, exerciseId = 101, setNumber = 1, weight = 80f, reps = 5)
        )
        val current = listOf(
            WorkoutSetEntity(id = 2, workoutId = 2, exerciseId = 101, setNumber = 1, weight = 85f, reps = 5)
        )

        val result = useCase(101, current, historical)
        assertTrue(result.isNewMaxWeightRecord)
        assertTrue(result.isNew1RMRecord)
    }
}
