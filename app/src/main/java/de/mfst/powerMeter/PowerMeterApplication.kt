package de.mfst.powerMeter

import android.app.Application
import de.mfst.powerMeter.data.PowerMeterDatabase

class PowerMeterApplication : Application() {
    val database: PowerMeterDatabase by lazy {
        PowerMeterDatabase.getInstance(this)
    }
}
