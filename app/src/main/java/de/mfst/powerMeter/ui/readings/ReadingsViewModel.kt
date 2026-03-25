package de.mfst.powerMeter.ui.readings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.MeterReading
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReadingGroup(
    val date: LocalDate,
    val readings: List<MeterReadingDisplay>
)

data class MeterReadingDisplay(
    val meterName: String,
    val reading: Double
)

data class ReadingsUiState(
    val readingGroups: List<ReadingGroup> = emptyList(),
    val meters: List<Meter> = emptyList(),
    val showAddDialog: Boolean = false
)

class ReadingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PowerMeterDatabase.getInstance(application)
    private val meterRepo = MeterRepository(db.meterDao())
    val meterReadingRepo = MeterReadingRepository(db.meterReadingDao())

    private val _showAddDialog = MutableStateFlow(false)

    val uiState: StateFlow<ReadingsUiState> = combine(
        meterReadingRepo.getAllReadings(),
        meterRepo.getAllMeters(),
        _showAddDialog
    ) { readings, meters, showDialog ->
        val meterMap = meters.associateBy { it.id }
        val groups = readings
            .groupBy { it.date }
            .map { (date, meterReadings) ->
                ReadingGroup(
                    date = date,
                    readings = meterReadings.map { r ->
                        MeterReadingDisplay(
                            meterName = meterMap[r.meterId]?.name ?: "Unknown",
                            reading = r.reading
                        )
                    }
                )
            }
            .sortedByDescending { it.date }

        ReadingsUiState(
            readingGroups = groups,
            meters = meters,
            showAddDialog = showDialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReadingsUiState())

    fun showAddDialog() { _showAddDialog.value = true }
    fun dismissAddDialog() { _showAddDialog.value = false }

    fun addReading(date: LocalDate, readings: Map<Long, Double>) {
        viewModelScope.launch {
            val meterReadings = readings.map { (meterId, value) ->
                MeterReading(meterId = meterId, date = date, reading = value)
            }
            meterReadingRepo.insertAll(meterReadings)
            _showAddDialog.value = false
        }
    }

    fun deleteReading(date: LocalDate) {
        viewModelScope.launch {
            meterReadingRepo.deleteByDate(date)
        }
    }
}
