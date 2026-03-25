package de.mfst.powerMeter.data.repository

import de.mfst.powerMeter.data.dao.MeterReadingDao
import de.mfst.powerMeter.data.entity.MeterReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MeterReadingRepository(private val meterReadingDao: MeterReadingDao) {
    fun getAllReadings(): Flow<List<MeterReading>> = meterReadingDao.getAllReadings()
    fun getAllReadingDates(): Flow<List<LocalDate>> = meterReadingDao.getAllReadingDates()
    suspend fun getReadingsForMeter(meterId: Long) = meterReadingDao.getReadingsForMeter(meterId)
    suspend fun getReadingAtOrBefore(meterId: Long, date: LocalDate) =
        meterReadingDao.getReadingAtOrBefore(meterId, date)
    suspend fun getReadingAtOrAfter(meterId: Long, date: LocalDate) =
        meterReadingDao.getReadingAtOrAfter(meterId, date)
    suspend fun insertAll(readings: List<MeterReading>) = meterReadingDao.insertAll(readings)
    suspend fun deleteByDate(date: LocalDate) = meterReadingDao.deleteByDate(date)
    suspend fun getAllReadingsForExport() = meterReadingDao.getAllReadingsForExport()
}
