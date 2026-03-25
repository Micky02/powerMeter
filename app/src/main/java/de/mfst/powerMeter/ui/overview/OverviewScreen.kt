package de.mfst.powerMeter.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

                // Total consumption card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
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

                // Per-meter breakdown
                if (state.meters.isNotEmpty()) {
                    Text("Meters", style = MaterialTheme.typography.titleSmall)
                    state.meters.forEach { meter ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(meter.meterName, style = MaterialTheme.typography.titleSmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Latest Reading")
                                    Text(
                                        meter.latestReading?.let { "${String.format("%.1f", it)} kWh" }
                                            ?: "No data"
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Consumption")
                                    Text(
                                        meter.consumptionKwh?.let { "${String.format("%.1f", it)} kWh" }
                                            ?: "No data"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
