package de.mfst.powerMeter.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.mfst.powerMeter.data.entity.MeterReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MeterReadingDao {
    @Query("SELECT * FROM meter_readings ORDER BY date DESC, meterId ASC")
    fun getAllReadings(): Flow<List<MeterReading>>

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId ORDER BY date ASC")
    suspend fun getReadingsForMeter(meterId: Long): List<MeterReading>

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND date <= :date ORDER BY date DESC LIMIT 1")
    suspend fun getReadingAtOrBefore(meterId: Long, date: LocalDate): MeterReading?

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND date >= :date ORDER BY date ASC LIMIT 1")
    suspend fun getReadingAtOrAfter(meterId: Long, date: LocalDate): MeterReading?

    @Query("SELECT DISTINCT date FROM meter_readings ORDER BY date DESC")
    fun getAllReadingDates(): Flow<List<LocalDate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<MeterReading>)

    @Query("DELETE FROM meter_readings WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)

    @Query("SELECT * FROM meter_readings ORDER BY date ASC, meterId ASC")
    suspend fun getAllReadingsForExport(): List<MeterReading>
}
