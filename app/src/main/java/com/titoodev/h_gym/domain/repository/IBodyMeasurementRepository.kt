package com.titoodev.h_gym.domain.repository

import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

interface IBodyMeasurementRepository {
    val measurementsFlow: Flow<List<BodyMeasurementEntity>>
    val latestMeasurementFlow: Flow<BodyMeasurementEntity?>
    fun getMeasurementsForRange(startTimestamp: Long, endTimestamp: Long): Flow<List<BodyMeasurementEntity>>
    suspend fun addMeasurement(measurement: BodyMeasurementEntity)
}
