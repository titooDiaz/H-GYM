package com.titoodev.h_gym

import com.titoodev.h_gym.util.DateUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun testStartAndEndOfDay() {
        val now = System.currentTimeMillis()
        val startOfDay = DateUtils.getStartOfDay(now)
        val endOfDay = DateUtils.getEndOfDay(now)

        assertTrue(startOfDay <= now)
        assertTrue(endOfDay >= now)
        assertTrue(endOfDay > startOfDay)

        val calStart = Calendar.getInstance().apply { timeInMillis = startOfDay }
        val calEnd = Calendar.getInstance().apply { timeInMillis = endOfDay }

        org.junit.Assert.assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))
        org.junit.Assert.assertEquals(0, calStart.get(Calendar.MINUTE))
        org.junit.Assert.assertEquals(0, calStart.get(Calendar.SECOND))

        org.junit.Assert.assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))
        org.junit.Assert.assertEquals(59, calEnd.get(Calendar.MINUTE))
        org.junit.Assert.assertEquals(59, calEnd.get(Calendar.SECOND))
    }
}
