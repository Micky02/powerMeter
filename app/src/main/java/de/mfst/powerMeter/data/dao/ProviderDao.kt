package de.mfst.powerMeter.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.entity.ProviderMeterInitialReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY startDate DESC")
    fun getAllProviders(): Flow<List<Provider>>

    @Query("SELECT * FROM providers WHERE endDate IS NULL LIMIT 1")
    fun getActiveProvider(): Flow<Provider?>

    @Query("SELECT * FROM providers WHERE endDate IS NULL LIMIT 1")
    suspend fun getActiveProviderOnce(): Provider?

    @Query("SELECT * FROM providers WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date) LIMIT 1")
    suspend fun getProviderForDate(date: LocalDate): Provider?

    @Insert
    suspend fun insert(provider: Provider): Long

    @Update
    suspend fun update(provider: Provider)

    @Insert
    suspend fun insertInitialReadings(readings: List<ProviderMeterInitialReading>)

    @Query("SELECT * FROM provider_meter_initial_readings WHERE providerId = :providerId")
    suspend fun getInitialReadingsForProvider(providerId: Long): List<ProviderMeterInitialReading>

    @Transaction
    suspend fun switchProvider(
        newProvider: Provider,
        initialReadings: List<ProviderMeterInitialReading>
    ): Long {
        val active = getActiveProviderOnce()
        if (active != null) {
            update(active.copy(endDate = newProvider.startDate.minusDays(1)))
        }
        val newId = insert(newProvider)
        val readingsWithId = initialReadings.map { it.copy(providerId = newId) }
        insertInitialReadings(readingsWithId)
        return newId
    }
}
