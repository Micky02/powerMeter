package de.mfst.powerMeter.ui.provider

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mfst.powerMeter.data.PowerMeterDatabase
import de.mfst.powerMeter.data.entity.Meter
import de.mfst.powerMeter.data.entity.Provider
import de.mfst.powerMeter.data.entity.ProviderMeterInitialReading
import de.mfst.powerMeter.data.repository.MeterRepository
import de.mfst.powerMeter.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProviderUiState(
    val providers: List<Provider> = emptyList(),
    val meters: List<Meter> = emptyList(),
    val showAddProviderDialog: Boolean = false,
    val showAddMeterDialog: Boolean = false
)

class ProviderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PowerMeterDatabase.getInstance(application)
    private val meterRepo = MeterRepository(db.meterDao())
    private val providerRepo = ProviderRepository(db.providerDao())

    private val _showAddProviderDialog = MutableStateFlow(false)
    private val _showAddMeterDialog = MutableStateFlow(false)

    val uiState: StateFlow<ProviderUiState> = combine(
        providerRepo.getAllProviders(),
        meterRepo.getAllMeters(),
        _showAddProviderDialog,
        _showAddMeterDialog
    ) { providers, meters, showProvider, showMeter ->
        ProviderUiState(
            providers = providers,
            meters = meters,
            showAddProviderDialog = showProvider,
            showAddMeterDialog = showMeter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProviderUiState())

    fun showAddProviderDialog() { _showAddProviderDialog.value = true }
    fun dismissAddProviderDialog() { _showAddProviderDialog.value = false }
    fun showAddMeterDialog() { _showAddMeterDialog.value = true }
    fun dismissAddMeterDialog() { _showAddMeterDialog.value = false }

    fun addMeter(name: String) {
        viewModelScope.launch {
            meterRepo.insert(Meter(name = name))
            _showAddMeterDialog.value = false
        }
    }

    fun deleteMeter(meter: Meter) {
        viewModelScope.launch {
            meterRepo.delete(meter)
        }
    }

    fun addProvider(
        provider: Provider,
        initialReadings: Map<Long, Double>
    ) {
        viewModelScope.launch {
            val readings = initialReadings.map { (meterId, reading) ->
                ProviderMeterInitialReading(
                    providerId = 0,
                    meterId = meterId,
                    initialReading = reading
                )
            }
            providerRepo.switchProvider(provider, readings)
            _showAddProviderDialog.value = false
        }
    }
}
