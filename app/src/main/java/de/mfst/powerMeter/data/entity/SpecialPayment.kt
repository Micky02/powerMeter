package de.mfst.powerMeter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "special_payments")
data class SpecialPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val date: LocalDate,
    val amountEur: Double,
    val note: String = ""
)
