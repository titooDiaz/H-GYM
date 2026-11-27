package com.titoodev.h_gym.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class Calculate1RMUseCaseTest {
    private val useCase = Calculate1RMUseCase()

    @Test
    fun `when reps is 1, return exact weight`() {
        val result = useCase(weight = 100f, reps = 1)
        assertEquals(100f, result, 0.01f)
    }

    @Test
    fun `when epley formula with 100kg for 10 reps, calculate 133_33`() {
        val result = useCase(weight = 100f, reps = 10, formula = OneRMFormula.EPLEY)
        assertEquals(133.33f, result, 0.05f)
    }

    @Test
    fun `when invalid input, return zero`() {
        assertEquals(0f, useCase(weight = -10f, reps = 5), 0.01f)
        assertEquals(0f, useCase(weight = 100f, reps = 0), 0.01f)
    }
}
