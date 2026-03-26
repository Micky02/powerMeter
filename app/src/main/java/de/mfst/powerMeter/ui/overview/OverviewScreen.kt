package de.mfst.powerMeter.ui.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Overview") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.hasProvider) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Welcome to PowerMeter",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Get started by adding meters and a provider in the Provider tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val provider = state.activeProvider!!

                // Provider info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Provider", style = MaterialTheme.typography.labelMedium)
                        Text(provider.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Since ${provider.startDate.format(dateFormatter)} | ${String.format("%.4f", provider.pricePerKwh)} EUR/kWh",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Consumption card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Consumption", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${String.format("%.1f", state.totalConsumptionKwh)} kWh",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                "since provider start",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.estimatedTodayConsumptionKwh != null) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Estimated Today", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${String.format("%.1f", state.estimatedTodayConsumptionKwh)} kWh",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                val dataNote = state.dataAsOfDate?.let { "as of ${it.format(dateFormatter)}" } ?: ""
                                Text(
                                    dataNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Running balance card
                val balance = state.totalBalanceEur
                if (balance != null) {
                    val balanceColor = if (balance >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Running Balance", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${if (balance >= 0) "+" else ""}${String.format("%.2f", balance)} EUR",
                                style = MaterialTheme.typography.headlineMedium,
                                color = balanceColor
                            )
                            Text(
                                if (balance >= 0) "surplus since contract start" else "deficit since contract start",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Current month cost
                if (state.hasData) {
                    val diffColor = if (state.monthlyInstallment >= state.currentMonthCostEur)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Current Month", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Cost")
                                Text("${String.format("%.2f", state.currentMonthCostEur)} EUR")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Installment")
                                Text("${String.format("%.2f", state.monthlyInstallment)} EUR")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (state.monthlyInstallment >= state.currentMonthCostEur) "Surplus" else "Deficit"
                                )
                                Text(
                                    "${String.format("%.2f", kotlin.math.abs(state.monthlyInstallment - state.currentMonthCostEur))} EUR",
                                    color = diffColor
                                )
                            }
                        }
                    }
                }

                // Monthly chart
                if (state.monthlyData.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly History", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Legend
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val tertiaryColor = MaterialTheme.colorScheme.tertiary
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        drawRect(color = primaryColor)
                                    }
                                    Text("kWh", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        drawCircle(color = tertiaryColor)
                                    }
                                    Text("EUR", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            ConsumptionCostChart(
                                data = state.monthlyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConsumptionCostChart(
    data: List<MonthlyDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)

    val maxConsumption = data.maxOf { it.consumptionKwh }.coerceAtLeast(0.001)
    val maxCost = data.maxOf { it.costEur }.coerceAtLeast(0.001)

    val monthFmt = remember { DateTimeFormatter.ofPattern("MMM") }
    val monthYearFmt = remember { DateTimeFormatter.ofPattern("MMM\nyy") }

    Canvas(modifier = modifier) {
        val lm = 44.dp.toPx()
        val rm = 48.dp.toPx()
        val bm = 30.dp.toPx()
        val tm = 4.dp.toPx()

        val cw = size.width - lm - rm
        val ch = size.height - bm - tm

        val n = data.size
        val slotW = cw / n
        val barW = (slotW * 0.55f).coerceAtLeast(3.dp.toPx())

        // Grid lines + y-axis labels
        for (i in 0..4) {
            val frac = i / 4f
            val y = tm + ch * (1f - frac)

            drawLine(gridColor, Offset(lm, y), Offset(lm + cw, y), 0.5.dp.toPx())

            val leftLabel = formatChartValue(maxConsumption * frac)
            val leftM = textMeasurer.measure(leftLabel, labelStyle)
            drawText(leftM, topLeft = Offset(lm - leftM.size.width - 3.dp.toPx(), y - leftM.size.height / 2f))

            val rightLabel = formatChartValue(maxCost * frac)
            val rightM = textMeasurer.measure(rightLabel, labelStyle)
            drawText(rightM, topLeft = Offset(lm + cw + 3.dp.toPx(), y - rightM.size.height / 2f))
        }

        val labelInterval = when {
            n <= 12 -> 1
            n <= 24 -> 2
            else -> 3
        }

        val linePoints = mutableListOf<Offset>()

        data.forEachIndexed { idx, point ->
            val cx = lm + slotW * idx + slotW / 2f

            // Consumption bar
            val barH = (point.consumptionKwh / maxConsumption * ch).toFloat().coerceAtLeast(0f)
            drawRect(
                color = if (point.isEstimated) primaryColor.copy(alpha = 0.35f) else primaryColor,
                topLeft = Offset(cx - barW / 2f, tm + ch - barH),
                size = Size(barW, barH)
            )

            // Cost line point
            val cy = tm + ch * (1f - (point.costEur / maxCost).toFloat()).coerceIn(0f, 1f)
            linePoints.add(Offset(cx, cy))

            // X-axis label
            if (idx % labelInterval == 0) {
                val showYear = idx == 0 || point.yearMonth.monthValue == 1
                val label = if (showYear)
                    point.yearMonth.format(monthYearFmt)
                else
                    point.yearMonth.format(monthFmt)
                val m = textMeasurer.measure(label, labelStyle)
                drawText(m, topLeft = Offset(cx - m.size.width / 2f, tm + ch + 3.dp.toPx()))
            }
        }

        // Cost line
        for (i in 0 until linePoints.size - 1) {
            drawLine(tertiaryColor, linePoints[i], linePoints[i + 1], 2.dp.toPx())
        }
        linePoints.forEach { pt ->
            drawCircle(tertiaryColor, 3.dp.toPx(), pt)
        }
    }
}

private fun formatChartValue(value: Double): String =
    if (value < 10.0) String.format("%.1f", value) else String.format("%.0f", value)
