package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateVolumeUseCaseTest {
    private val useCase = CalculateVolumeUseCase()

    @Test
    fun `calculate total volume correctly`() {
        val sets = listOf(
            WorkoutSetEntity(id = 1, workoutId = 1, exerciseId = 101, setNumber = 1, weight = 100f, reps = 10),
            WorkoutSetEntity(id = 2, workoutId = 1, exerciseId = 101, setNumber = 2, weight = 100f, reps = 8)
        )
        val result = useCase(sets)
        assertEquals(1800f, result, 0.01f)
    }

    @Test
    fun `ignore sets with zero weight or reps`() {
        val sets = listOf(
            WorkoutSetEntity(id = 1, workoutId = 1, exerciseId = 101, setNumber = 1, weight = 0f, reps = 10),
            WorkoutSetEntity(id = 2, workoutId = 1, exerciseId = 101, setNumber = 2, weight = 80f, reps = 5)
        )
        val result = useCase(sets)
        assertEquals(400f, result, 0.01f)
    }
}
