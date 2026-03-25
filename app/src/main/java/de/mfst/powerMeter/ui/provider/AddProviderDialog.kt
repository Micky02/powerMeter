package de.mfst.powerMeter.ui.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.Provider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProviderDialog(
    meters: List<Meter>,
    onDismiss: () -> Unit,
    onConfirm: (Provider, Map<Long, Double>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pricePerKwh by remember { mutableStateOf("") }
    var monthlyBaseFee by remember { mutableStateOf("") }
    var monthlyInstallment by remember { mutableStateOf("") }
    val initialReadings = remember { mutableStateMapOf<Long, String>() }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.of("UTC"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Provider") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startDate.format(dateFormatter),
                        onValueChange = {},
                        label = { Text("Start Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Pick")
                    }
                }

                OutlinedTextField(
                    value = pricePerKwh,
                    onValueChange = { pricePerKwh = it },
                    label = { Text("Price per kWh (EUR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlyBaseFee,
                    onValueChange = { monthlyBaseFee = it },
                    label = { Text("Monthly Base Fee (EUR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlyInstallment,
                    onValueChange = { monthlyInstallment = it },
                    label = { Text("Monthly Installment (EUR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (meters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Initial Meter Readings")
                    meters.forEach { meter ->
                        OutlinedTextField(
                            value = initialReadings[meter.id] ?: "",
                            onValueChange = { initialReadings[meter.id] = it },
                            label = { Text(meter.name) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val provider = Provider(
                        name = name,
                        startDate = startDate,
                        pricePerKwh = pricePerKwh.toDoubleOrNull() ?: 0.0,
                        monthlyBaseFee = monthlyBaseFee.toDoubleOrNull() ?: 0.0,
                        monthlyInstallment = monthlyInstallment.toDoubleOrNull() ?: 0.0
                    )
                    val readings = initialReadings.mapValues { (_, v) -> v.toDoubleOrNull() ?: 0.0 }
                    onConfirm(provider, readings)
                },
                enabled = name.isNotBlank() && pricePerKwh.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
