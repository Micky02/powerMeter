package de.mfst.powerMeter.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "meter_readings",
    primaryKeys = ["meterId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Meter::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("date")]
)
data class MeterReading(
    val meterId: Long,
    val date: LocalDate,
    val reading: Double
)
