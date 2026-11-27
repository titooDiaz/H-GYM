package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateChallengeProgressUseCaseTest {
    private val useCase = CalculateChallengeProgressUseCase()

    @Test
    fun `calculate challenge progress correctly`() {
        val now = System.currentTimeMillis()
        val workouts = listOf(
            WorkoutEntity(id = 1, title = "Sesión 1", date = now - 1000, durationSeconds = 1800, status = "COMPLETED"),
            WorkoutEntity(id = 2, title = "Sesión 2", date = now - 500, durationSeconds = 1800, status = "COMPLETED")
        )

        val result = useCase(
            targetCount = 2,
            workouts = workouts,
            startTimestamp = now - 5000,
            endTimestamp = now + 5000
        )

        assertEquals(2, result.completedWorkoutsCount)
        assertEquals(100f, result.progressPercentage, 0.01f)
        assertTrue(result.isCompleted)
    }
}
