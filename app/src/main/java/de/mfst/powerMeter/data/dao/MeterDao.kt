package de.mfst.powerMeter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.mfst.powerMeter.data.entity.Meter
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    @Query("SELECT * FROM meters ORDER BY name")
    fun getAllMeters(): Flow<List<Meter>>

    @Query("SELECT * FROM meters ORDER BY name")
    suspend fun getAllMetersList(): List<Meter>

    @Insert
    suspend fun insert(meter: Meter): Long

    @Update
    suspend fun update(meter: Meter)

    @Delete
    suspend fun delete(meter: Meter)
}
