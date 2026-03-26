package de.mfst.powerMeter.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import de.mfst.powerMeter.data.repository.ProviderRepository
import de.mfst.powerMeter.data.repository.SpecialPaymentRepository
import de.mfst.powerMeter.ui.bill.BillCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class MeterOverview(
    val meterName: String,
    val latestReading: Double?,
    val latestReadingDate: LocalDate?,
    val consumptionKwh: Double?,
    val estimatedTodayKwh: Double?
)

data class MonthlyDataPoint(
    val yearMonth: YearMonth,
    val consumptionKwh: Double,
    val costEur: Double,
    val isEstimated: Boolean
)

data class OverviewUiState(
    val activeProvider: Provider? = null,
    val totalConsumptionKwh: Double = 0.0,
    val estimatedTodayConsumptionKwh: Double? = null,
    val dataAsOfDate: LocalDate? = null,
    val totalBalanceEur: Double? = null,
    val currentMonthCostEur: Double = 0.0,
    val monthlyInstallment: Double = 0.0,
    val meters: List<MeterOverview> = emptyList(),
    val monthlyData: List<MonthlyDataPoint> = emptyList(),
    val hasProvider: Boolean = false,
    val hasData: Boolean = false
)

class OverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PowerMeterDatabase.getInstance(application)
    private val providerRepo = ProviderRepository(db.providerDao())
    private val meterRepo = MeterRepository(db.meterDao())
    private val meterReadingRepo = MeterReadingRepository(db.meterReadingDao())
    private val specialPaymentRepo = SpecialPaymentRepository(db.specialPaymentDao())
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

            val today = LocalDate.now()
            var totalConsumption = 0.0
            val meterOverviews = meters.map { meter ->
                val readings = meterReadingRepo.getReadingsForMeter(meter.id)
                val latestEntry = readings.lastOrNull()
                val latestReading = latestEntry?.reading
                val latestReadingDate = latestEntry?.date
                val initialReading = initialMap[meter.id]?.initialReading
                val consumption = if (latestReading != null && initialReading != null) {
                    (latestReading - initialReading).coerceAtLeast(0.0)
                } else null

                if (consumption != null) totalConsumption += consumption

                val estimatedTodayKwh = if (consumption != null && latestReadingDate != null) {
                    val daysSinceStart = ChronoUnit.DAYS.between(provider.startDate, latestReadingDate)
                    val daysToToday = ChronoUnit.DAYS.between(provider.startDate, today)
                    if (daysSinceStart > 0 && daysToToday >= 0) {
                        consumption / daysSinceStart * daysToToday
                    } else {
                        consumption
                    }
                } else null

                MeterOverview(
                    meterName = meter.name,
                    latestReading = latestReading,
                    latestReadingDate = latestReadingDate,
                    consumptionKwh = consumption,
                    estimatedTodayKwh = estimatedTodayKwh
                )
            }

            val estimatedTodayTotal = if (meterOverviews.all { it.estimatedTodayKwh != null })
                meterOverviews.sumOf { it.estimatedTodayKwh!! }
            else null
            val dataAsOfDate = meterOverviews.mapNotNull { it.latestReadingDate }.maxOrNull()

            // Calculate running balance since contract start
            val totalBalanceEur = if (estimatedTodayTotal != null) {
                val monthsElapsed = ChronoUnit.MONTHS.between(
                    provider.startDate.withDayOfMonth(1),
                    today.withDayOfMonth(1)
                ) + 1
                val totalCost = estimatedTodayTotal * provider.pricePerKwh +
                        provider.monthlyBaseFee * monthsElapsed
                val specialPayments = specialPaymentRepo.getPaymentsForProviderOnce(provider.id)
                val totalPaid = provider.monthlyInstallment * monthsElapsed +
                        specialPayments.sumOf { it.amountEur }
                totalPaid - totalCost
            } else null

            // Build monthly data series for the chart
            val currentMonth = YearMonth.now()
            val monthlyData = mutableListOf<MonthlyDataPoint>()
            var m = YearMonth.from(provider.startDate)
            while (!m.isAfter(currentMonth)) {
                val bill = calculator.calculateMonthlyBill(m, meters, provider)
                if (bill != null) {
                    monthlyData.add(
                        MonthlyDataPoint(
                            yearMonth = m,
                            consumptionKwh = bill.consumptionKwh,
                            costEur = bill.totalCostEur,
                            isEstimated = m == currentMonth
                        )
                    )
                }
                m = m.plusMonths(1)
            }

            val billResult = monthlyData.lastOrNull { !it.isEstimated }
                ?.let { calculator.calculateMonthlyBill(it.yearMonth, meters, provider) }
                ?: monthlyData.lastOrNull()?.let { calculator.calculateMonthlyBill(it.yearMonth, meters, provider) }

            _uiState.value = OverviewUiState(
                activeProvider = provider,
                totalConsumptionKwh = totalConsumption,
                estimatedTodayConsumptionKwh = estimatedTodayTotal,
                dataAsOfDate = dataAsOfDate,
                totalBalanceEur = totalBalanceEur,
                currentMonthCostEur = monthlyData.lastOrNull()?.costEur ?: 0.0,
                monthlyInstallment = provider.monthlyInstallment,
                meters = meterOverviews,
                monthlyData = monthlyData,
                hasProvider = true,
                hasData = monthlyData.isNotEmpty()
            )
        }
    }
}
