package de.mfst.powerMeter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.mfst.powerMeter.data.dao.MeterDao
import de.mfst.powerMeter.data.dao.MeterReadingDao
import de.mfst.powerMeter.data.dao.ProviderDao
import de.mfst.powerMeter.data.dao.SpecialPaymentDao
import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.MeterReading
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.entity.ProviderMeterInitialReading
import de.mfst.powerMeter.data.entity.SpecialPayment

@Database(
    entities = [
        Meter::class,
        Provider::class,
        ProviderMeterInitialReading::class,
        MeterReading::class,
        SpecialPayment::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PowerMeterDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun providerDao(): ProviderDao
    abstract fun meterReadingDao(): MeterReadingDao
    abstract fun specialPaymentDao(): SpecialPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: PowerMeterDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS special_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        providerId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        amountEur REAL NOT NULL,
                        note TEXT NOT NULL DEFAULT ''
                    )"""
                )
            }
        }

        fun getInstance(context: Context): PowerMeterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PowerMeterDatabase::class.java,
                    "power_meter.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}
