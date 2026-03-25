package de.mfst.powerMeter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.mfst.powerMeter.data.entity.SpecialPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialPaymentDao {
    @Query("SELECT * FROM special_payments WHERE providerId = :providerId ORDER BY date DESC")
    fun getPaymentsForProvider(providerId: Long): Flow<List<SpecialPayment>>

    @Query("SELECT * FROM special_payments WHERE providerId = :providerId ORDER BY date DESC")
    suspend fun getPaymentsForProviderOnce(providerId: Long): List<SpecialPayment>

    @Insert
    suspend fun insert(payment: SpecialPayment)

    @Delete
    suspend fun delete(payment: SpecialPayment)
}
