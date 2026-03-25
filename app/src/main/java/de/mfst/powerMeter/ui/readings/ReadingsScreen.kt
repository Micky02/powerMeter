package de.mfst.powerMeter.ui.readings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mfst.powerMeter.data.csv.CsvManager
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsScreen(
    viewModel: ReadingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val csvManager = remember(context) {
        val db = PowerMeterDatabase.getInstance(context)
        CsvManager(
            MeterReadingRepository(db.meterReadingDao()),
            MeterRepository(db.meterDao())
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                exportCsv(context, csvManager, it)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                importCsv(context, csvManager, it)
            }
        }
    }

    if (state.showAddDialog) {
        AddReadingDialog(
            meters = state.meters,
            onDismiss = viewModel::dismissAddDialog,
            onConfirm = { date, readings -> viewModel.addReading(date, readings) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meter Readings") },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import CSV")
                    }
                    IconButton(onClick = { exportLauncher.launch("meter_readings.csv") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.meters.isNotEmpty()) {
                FloatingActionButton(onClick = viewModel::showAddDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reading")
                }
            }
        }
    ) { padding ->
        if (state.meters.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No meters defined yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Go to the Provider tab to add meters first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (state.readingGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No readings yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Tap + to add your first meter reading.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(state.readingGroups, key = { it.date.toEpochDay() }) { group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            group.date.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(2f)) {
                            group.readings.forEach { reading ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reading.meterName, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format("%.1f", reading.reading)} kWh",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { viewModel.deleteReading(group.date) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider()
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

private suspend fun exportCsv(context: Context, csvManager: CsvManager, uri: Uri) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            csvManager.exportToCsv(stream)
        }
    }
    withContext(Dispatchers.Main) {
        Toast.makeText(context, "Export complete", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun importCsv(context: Context, csvManager: CsvManager, uri: Uri) {
    val result = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            csvManager.importFromCsv(stream)
        }
    }
    withContext(Dispatchers.Main) {
        val msg = if (result != null) {
            "Imported ${result.importedCount} readings" +
                    if (result.errors.isNotEmpty()) " (${result.errors.size} errors)" else ""
        } else "Import failed"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
