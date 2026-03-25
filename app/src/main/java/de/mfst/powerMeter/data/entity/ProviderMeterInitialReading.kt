package de.mfst.powerMeter.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "provider_meter_initial_readings",
    primaryKeys = ["providerId", "meterId"],
    foreignKeys = [
        ForeignKey(
            entity = Provider::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Meter::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meterId")]
)
data class ProviderMeterInitialReading(
    val providerId: Long,
    val meterId: Long,
    val initialReading: Double
)
