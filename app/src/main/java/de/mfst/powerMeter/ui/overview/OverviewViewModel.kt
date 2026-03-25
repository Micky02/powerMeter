package de.mfst.powerMeter.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import de.mfst.powerMeter.data.repository.ProviderRepository
import de.mfst.powerMeter.ui.bill.BillCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class MeterOverview(
    val meterName: String,
    val latestReading: Double?,
    val consumptionKwh: Double?
)

data class OverviewUiState(
    val activeProvider: Provider? = null,
    val totalConsumptionKwh: Double = 0.0,
    val currentMonthCostEur: Double = 0.0,
    val monthlyInstallment: Double = 0.0,
    val meters: List<MeterOverview> = emptyList(),
    val hasProvider: Boolean = false,
    val hasData: Boolean = false
)

class OverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PowerMeterDatabase.getInstance(application)
    private val providerRepo = ProviderRepository(db.providerDao())
    private val meterRepo = MeterRepository(db.meterDao())
    private val meterReadingRepo = MeterReadingRepository(db.meterReadingDao())
    private val calculator = BillCalculator(meterReadingRepo)

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState

    init {
        loadData()
    }

    fun refresh() { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val provider = providerRepo.getActiveProvider().first()
            if (provider == null) {
                _uiState.value = OverviewUiState(hasProvider = false)
                return@launch
            }

            val meters = meterRepo.getAllMeters().first()
            val initialReadings = providerRepo.getInitialReadingsForProvider(provider.id)
            val initialMap = initialReadings.associateBy { it.meterId }

            var totalConsumption = 0.0
            val meterOverviews = meters.map { meter ->
                val readings = meterReadingRepo.getReadingsForMeter(meter.id)
                val latestReading = readings.lastOrNull()?.reading
                val initialReading = initialMap[meter.id]?.initialReading
                val consumption = if (latestReading != null && initialReading != null) {
                    (latestReading - initialReading).coerceAtLeast(0.0)
                } else null

                if (consumption != null) totalConsumption += consumption

                MeterOverview(
                    meterName = meter.name,
                    latestReading = latestReading,
                    consumptionKwh = consumption
                )
            }

            // Calculate current month cost
            val currentMonth = YearMonth.now()
            val billResult = calculator.calculateMonthlyBill(currentMonth, meters, provider)

            _uiState.value = OverviewUiState(
                activeProvider = provider,
                totalConsumptionKwh = totalConsumption,
                currentMonthCostEur = billResult?.totalCostEur ?: 0.0,
                monthlyInstallment = provider.monthlyInstallment,
                meters = meterOverviews,
                hasProvider = true,
                hasData = billResult != null
            )
        }
    }
}
