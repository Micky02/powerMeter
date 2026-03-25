package de.mfst.powerMeter.ui.bill

import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class BillResult(
    val consumptionKwh: Double,
    val energyCostEur: Double,
    val baseFeeEur: Double,
    val totalCostEur: Double,
    val installmentEur: Double,
    val differenceEur: Double
)

class BillCalculator(private val meterReadingRepo: MeterReadingRepository) {

    suspend fun estimateReadingAt(meterId: Long, targetDate: LocalDate): Double? {
        val before = meterReadingRepo.getReadingAtOrBefore(meterId, targetDate)
        val after = meterReadingRepo.getReadingAtOrAfter(meterId, targetDate)

        // Exact match
        if (before != null && before.date == targetDate) return before.reading

        // Interpolation
        if (before != null && after != null) {
            val totalDays = ChronoUnit.DAYS.between(before.date, after.date).toDouble()
            val daysFromBefore = ChronoUnit.DAYS.between(before.date, targetDate).toDouble()
            return before.reading + (after.reading - before.reading) * (daysFromBefore / totalDays)
        }

        // Extrapolation forward
        if (before != null) {
            val secondBefore = meterReadingRepo.getReadingAtOrBefore(meterId, before.date.minusDays(1))
            if (secondBefore != null) {
                val daysBetween = ChronoUnit.DAYS.between(secondBefore.date, before.date).toDouble()
                if (daysBetween > 0) {
                    val dailyRate = (before.reading - secondBefore.reading) / daysBetween
                    val daysForward = ChronoUnit.DAYS.between(before.date, targetDate).toDouble()
                    return before.reading + dailyRate * daysForward
                }
            }
            return null
        }

        // Extrapolation backward
        if (after != null) {
            val secondAfter = meterReadingRepo.getReadingAtOrAfter(meterId, after.date.plusDays(1))
            if (secondAfter != null) {
                val daysBetween = ChronoUnit.DAYS.between(after.date, secondAfter.date).toDouble()
                if (daysBetween > 0) {
                    val dailyRate = (secondAfter.reading - after.reading) / daysBetween
                    val daysBackward = ChronoUnit.DAYS.between(targetDate, after.date).toDouble()
                    return after.reading - dailyRate * daysBackward
                }
            }
            return null
        }

        return null
    }

    suspend fun calculateMonthlyBill(
        yearMonth: YearMonth,
        meters: List<Meter>,
        provider: Provider
    ): BillResult? {
        if (meters.isEmpty()) return null

        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.plusMonths(1).atDay(1)

        var totalConsumption = 0.0
        for (meter in meters) {
            val readingStart = estimateReadingAt(meter.id, monthStart) ?: return null
            val readingEnd = estimateReadingAt(meter.id, monthEnd) ?: return null
            totalConsumption += (readingEnd - readingStart)
        }

        val energyCost = totalConsumption * provider.pricePerKwh
        val baseFee = provider.monthlyBaseFee
        val totalCost = energyCost + baseFee

        return BillResult(
            consumptionKwh = totalConsumption,
            energyCostEur = energyCost,
            baseFeeEur = baseFee,
            totalCostEur = totalCost,
            installmentEur = provider.monthlyInstallment,
            differenceEur = provider.monthlyInstallment - totalCost
        )
    }
}
