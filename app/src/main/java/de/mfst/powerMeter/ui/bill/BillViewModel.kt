package de.mfst.powerMeter.ui.bill

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import de.mfst.powerMeter.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BillUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val consumptionKwh: Double = 0.0,
    val energyCostEur: Double = 0.0,
    val baseFeeEur: Double = 0.0,
    val totalCostEur: Double = 0.0,
    val installmentEur: Double = 0.0,
    val differenceEur: Double = 0.0,
    val hasData: Boolean = false,
    val noProvider: Boolean = false
)

class BillViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PowerMeterDatabase.getInstance(application)
    private val providerRepo = ProviderRepository(db.providerDao())
    private val meterRepo = MeterRepository(db.meterDao())
    private val meterReadingRepo = MeterReadingRepository(db.meterReadingDao())
    private val calculator = BillCalculator(meterReadingRepo)

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState

    init {
        calculateBill()
    }

    fun selectMonth(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = yearMonth)
        calculateBill()
    }

    fun previousMonth() {
        selectMonth(_uiState.value.selectedMonth.minusMonths(1))
    }

    fun nextMonth() {
        selectMonth(_uiState.value.selectedMonth.plusMonths(1))
    }

    private fun calculateBill() {
        viewModelScope.launch {
            val month = _uiState.value.selectedMonth
            val provider = providerRepo.getProviderForDate(month.atDay(15))

            if (provider == null) {
                _uiState.value = _uiState.value.copy(hasData = false, noProvider = true)
                return@launch
            }

            val meters = meterRepo.getAllMeters().first()
            val result = calculator.calculateMonthlyBill(month, meters, provider)

            if (result == null) {
                _uiState.value = _uiState.value.copy(hasData = false, noProvider = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    consumptionKwh = result.consumptionKwh,
                    energyCostEur = result.energyCostEur,
                    baseFeeEur = result.baseFeeEur,
                    totalCostEur = result.totalCostEur,
                    installmentEur = result.installmentEur,
                    differenceEur = result.differenceEur,
                    hasData = true,
                    noProvider = false
                )
            }
        }
    }
}
