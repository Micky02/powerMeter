package de.mfst.powerMeter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.mfst.powerMeter.data.dao.MeterDao
import de.mfst.powerMeter.data.dao.MeterReadingDao
import de.mfst.powerMeter.data.dao.ProviderDao
import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.MeterReading
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.entity.ProviderMeterInitialReading

@Database(
    entities = [
        Meter::class,
        Provider::class,
        ProviderMeterInitialReading::class,
        MeterReading::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PowerMeterDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun providerDao(): ProviderDao
    abstract fun meterReadingDao(): MeterReadingDao

    companion object {
        @Volatile
        private var INSTANCE: PowerMeterDatabase? = null

        fun getInstance(context: Context): PowerMeterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PowerMeterDatabase::class.java,
                    "power_meter.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
