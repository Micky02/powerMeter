package de.mfst.powerMeter.ui.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScreen(
    viewModel: ProviderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    if (state.showAddMeterDialog) {
        AddMeterDialog(
            onDismiss = viewModel::dismissAddMeterDialog,
            onConfirm = viewModel::addMeter
        )
    }

    if (state.showAddProviderDialog) {
        AddProviderDialog(
            meters = state.meters,
            onDismiss = viewModel::dismissAddProviderDialog,
            onConfirm = { provider, readings -> viewModel.addProvider(provider, readings) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Provider") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Meters section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Meters", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = viewModel::showAddMeterDialog) {
                        Icon(Icons.Default.Add, contentDescription = "Add Meter")
                    }
                }
            }

            if (state.meters.isEmpty()) {
                item {
                    Text(
                        "No meters defined. Add meters before adding a provider.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.meters, key = { it.id }) { meter ->
                ListItem(
                    headlineContent = { Text(meter.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteMeter(meter) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }

            // Divider
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add Provider button
            item {
                Button(
                    onClick = viewModel::showAddProviderDialog,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.meters.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("  Add Provider")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Provider history
            val activeProvider = state.providers.firstOrNull { it.endDate == null }
            if (activeProvider != null) {
                item {
                    Text("Current Provider", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(activeProvider.name, style = MaterialTheme.typography.titleSmall)
                            Text("Since: ${activeProvider.startDate.format(dateFormatter)}")
                            Text("Price: ${String.format("%.4f", activeProvider.pricePerKwh)} EUR/kWh")
                            Text("Base Fee: ${String.format("%.2f", activeProvider.monthlyBaseFee)} EUR/month")
                            Text("Installment: ${String.format("%.2f", activeProvider.monthlyInstallment)} EUR/month")
                        }
                    }
                }
            }

            val pastProviders = state.providers.filter { it.endDate != null }
            if (pastProviders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Provider History", style = MaterialTheme.typography.titleMedium)
                }
                items(pastProviders, key = { it.id }) { provider ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(provider.name, style = MaterialTheme.typography.titleSmall)
                            Text("${provider.startDate.format(dateFormatter)} - ${provider.endDate?.format(dateFormatter)}")
                            Text("Price: ${String.format("%.4f", provider.pricePerKwh)} EUR/kWh")
                            Text("Base Fee: ${String.format("%.2f", provider.monthlyBaseFee)} EUR/month")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AddMeterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Meter") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Meter Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
