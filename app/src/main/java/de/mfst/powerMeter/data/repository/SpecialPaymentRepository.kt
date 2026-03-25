package de.mfst.powerMeter.data.repository

import de.mfst.powerMeter.data.dao.SpecialPaymentDao
import de.mfst.powerMeter.data.entity.SpecialPayment
import kotlinx.coroutines.flow.Flow

class SpecialPaymentRepository(private val dao: SpecialPaymentDao) {
    fun getPaymentsForProvider(providerId: Long): Flow<List<SpecialPayment>> =
        dao.getPaymentsForProvider(providerId)

    suspend fun getPaymentsForProviderOnce(providerId: Long): List<SpecialPayment> =
        dao.getPaymentsForProviderOnce(providerId)

    suspend fun insert(payment: SpecialPayment) = dao.insert(payment)

    suspend fun delete(payment: SpecialPayment) = dao.delete(payment)
}
