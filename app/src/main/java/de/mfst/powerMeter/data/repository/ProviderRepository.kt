package de.mfst.powerMeter.data.repository

import de.mfst.powerMeter.data.dao.ProviderDao
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.entity.ProviderMeterInitialReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ProviderRepository(private val providerDao: ProviderDao) {
    fun getAllProviders(): Flow<List<Provider>> = providerDao.getAllProviders()
    fun getActiveProvider(): Flow<Provider?> = providerDao.getActiveProvider()
    suspend fun getProviderForDate(date: LocalDate): Provider? = providerDao.getProviderForDate(date)
    suspend fun getInitialReadingsForProvider(providerId: Long) =
        providerDao.getInitialReadingsForProvider(providerId)

    suspend fun switchProvider(
        newProvider: Provider,
        initialReadings: List<ProviderMeterInitialReading>
    ): Long = providerDao.switchProvider(newProvider, initialReadings)
}
