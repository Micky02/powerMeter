package de.mfst.powerMeter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "providers")
data class Provider(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val pricePerKwh: Double,
    val monthlyBaseFee: Double,
    val monthlyInstallment: Double
)
