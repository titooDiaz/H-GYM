package com.titoodev.h_gym.domain.usecase

import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity

data class PersonalRecordResult(
    val exerciseId: Long,
    val maxWeight: Float,
    val max1RM: Float,
    val isNewMaxWeightRecord: Boolean,
    val isNew1RMRecord: Boolean
)

class DetectPersonalRecordsUseCase(
    private val calculate1RMUseCase: Calculate1RMUseCase = Calculate1RMUseCase()
) {
    operator fun invoke(
        exerciseId: Long,
        currentSets: List<WorkoutSetEntity>,
        historicalSets: List<WorkoutSetEntity>
    ): PersonalRecordResult {
        val historicalMaxWeight = historicalSets.maxOfOrNull { it.weight } ?: 0f
        val historicalMax1RM = historicalSets.maxOfOrNull {
            calculate1RMUseCase(it.weight, it.reps)
        } ?: 0f

        val currentMaxWeight = currentSets.maxOfOrNull { it.weight } ?: 0f
        val currentMax1RM = currentSets.maxOfOrNull {
            calculate1RMUseCase(it.weight, it.reps)
        } ?: 0f

        val isNewMaxWeightRecord = currentMaxWeight > historicalMaxWeight && currentMaxWeight > 0f
        val isNew1RMRecord = currentMax1RM > historicalMax1RM && currentMax1RM > 0f

        return PersonalRecordResult(
            exerciseId = exerciseId,
            maxWeight = maxOf(currentMaxWeight, historicalMaxWeight),
            max1RM = maxOf(currentMax1RM, historicalMax1RM),
            isNewMaxWeightRecord = isNewMaxWeightRecord,
            isNew1RMRecord = isNew1RMRecord
        )
    }
}
