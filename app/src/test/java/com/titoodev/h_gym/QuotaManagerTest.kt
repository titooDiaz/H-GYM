package com.titoodev.h_gym

import com.titoodev.h_gym.data.local.entities.FirebaseQuotaEntity
import com.titoodev.h_gym.data.sync.QuotaManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotaManagerTest {

    @Test
    fun testQuotaManagerPreventiveThresholds() {
        val normalQuota = FirebaseQuotaEntity(
            estimatedWritesToday = 5000,
            estimatedReadsToday = 10000,
            isWriteQuotaPaused = false
        )
        assertTrue(QuotaManager.isWriteSyncAllowed(normalQuota))
        assertTrue(QuotaManager.isReadSyncAllowed(normalQuota))

        val highWriteQuota = FirebaseQuotaEntity(
            estimatedWritesToday = 16000, // > 15,000 threshold (75% of 20k)
            estimatedReadsToday = 10000
        )
        assertFalse(QuotaManager.isWriteSyncAllowed(highWriteQuota))

        val highReadQuota = FirebaseQuotaEntity(
            estimatedWritesToday = 1000,
            estimatedReadsToday = 38000 // > 37,500 threshold (75% of 50k)
        )
        assertFalse(QuotaManager.isReadSyncAllowed(highReadQuota))
    }
}
