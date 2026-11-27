package com.titoodev.h_gym.domain.usecase

enum class OneRMFormula {
    EPLEY,
    BRZYCKI
}

class Calculate1RMUseCase {
    operator fun invoke(
        weight: Float,
        reps: Int,
        formula: OneRMFormula = OneRMFormula.EPLEY
    ): Float {
        if (weight <= 0f || reps <= 0) return 0f
        if (reps == 1) return weight

        return when (formula) {
            OneRMFormula.EPLEY -> weight * (1f + reps.toFloat() / 30f)
            OneRMFormula.BRZYCKI -> {
                if (reps >= 37) weight
                else weight * (36f / (37f - reps.toFloat()))
            }
        }
    }
}
