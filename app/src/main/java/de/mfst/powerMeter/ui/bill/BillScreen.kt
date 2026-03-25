package de.mfst.powerMeter.ui.bill

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    viewModel: BillViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Electricity Bill") })
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
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                }
                Text(
                    state.selectedMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                }
            }

            if (state.noProvider) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No provider configured for this month.\nGo to the Provider tab to add one.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (!state.hasData) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Not enough meter readings to calculate the bill for this month.\nAdd more readings in the Readings tab.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Consumption breakdown
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cost Breakdown", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        BillRow("Consumption", "${String.format("%.1f", state.consumptionKwh)} kWh")
                        BillRow("Energy Cost", "${String.format("%.2f", state.energyCostEur)} EUR")
                        BillRow("Base Fee", "${String.format("%.2f", state.baseFeeEur)} EUR")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        BillRow("Total Cost", "${String.format("%.2f", state.totalCostEur)} EUR")
                    }
                }

                // Comparison with installment
                val diffColor = if (state.differenceEur >= 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Installment Comparison", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        BillRow("Monthly Installment", "${String.format("%.2f", state.installmentEur)} EUR")
                        BillRow("Actual Cost", "${String.format("%.2f", state.totalCostEur)} EUR")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (state.differenceEur >= 0) "Surplus" else "Deficit",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${String.format("%.2f", kotlin.math.abs(state.differenceEur))} EUR",
                                style = MaterialTheme.typography.bodyMedium,
                                color = diffColor
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
private fun BillRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
