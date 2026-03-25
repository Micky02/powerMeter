package de.mfst.powerMeter.data.repository

import de.mfst.powerMeter.data.dao.MeterDao
import de.mfst.powerMeter.data.entity.Meter
import kotlinx.coroutines.flow.Flow

class MeterRepository(private val meterDao: MeterDao) {
    fun getAllMeters(): Flow<List<Meter>> = meterDao.getAllMeters()
    suspend fun getAllMetersList(): List<Meter> = meterDao.getAllMetersList()
    suspend fun insert(meter: Meter): Long = meterDao.insert(meter)
    suspend fun update(meter: Meter) = meterDao.update(meter)
    suspend fun delete(meter: Meter) = meterDao.delete(meter)
}
