package de.mfst.powerMeter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meters")
data class Meter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
