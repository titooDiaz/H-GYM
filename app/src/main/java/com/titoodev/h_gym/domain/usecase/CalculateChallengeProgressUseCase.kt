package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutEntity

data class ChallengeProgressResult(
    val targetWorkoutsCount: Int,
    val completedWorkoutsCount: Int,
    val progressPercentage: Float,
    val isCompleted: Boolean
)

class CalculateChallengeProgressUseCase {
    operator fun invoke(
        targetCount: Int,
        workouts: List<WorkoutEntity>,
        startTimestamp: Long,
        endTimestamp: Long
    ): ChallengeProgressResult {
        val completedInWindow = workouts.count {
            it.date in startTimestamp..endTimestamp && it.status == "COMPLETED"
        }
        val safeTarget = targetCount.coerceAtLeast(1)
        val percentage = (completedInWindow.toFloat() / safeTarget.toFloat() * 100f).coerceIn(0f, 100f)

        return ChallengeProgressResult(
            targetWorkoutsCount = safeTarget,
            completedWorkoutsCount = completedInWindow,
            progressPercentage = percentage,
            isCompleted = completedInWindow >= safeTarget
        )
    }
}
