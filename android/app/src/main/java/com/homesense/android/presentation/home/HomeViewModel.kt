package com.homesense.android.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homesense.android.domain.model.EnergyRestoredNotification
import com.homesense.android.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh(DEFAULT_DEVICE_ID)
    }

    fun refresh(deviceId: String = DEFAULT_DEVICE_ID) {
        viewModelScope.launch {
            _uiState.update { s ->
                s.copy(isLoading = true, errorMessage = null)
            }
            val previousRestored = _uiState.value.energyHistory.filterIsInstance<EnergyHistoryUiItem.EnergyRestoredCard>()
            runCatching { dashboardRepository.loadDashboard(deviceId) }
                .onSuccess { snap ->
                    val fromApi = snap.outageStartsIso.map { EnergyHistoryUiItem.RecordedOutage(it) }
                    _uiState.update { current ->
                        HomeDashboardUiState(
                            isLoading = false,
                            errorMessage = null,
                            temperatureC = snap.temperatureC ?: current.temperatureC,
                            humidityPercent = snap.humidityPercent ?: current.humidityPercent,
                            climateSubtitle = snap.climateRecordedAt?.let { "Atualizado: $it" }
                                ?: current.climateSubtitle,
                            selectedTab = current.selectedTab,
                            energyHistory = previousRestored + fromApi,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Falha ao carregar",
                        )
                    }
                }
        }
    }

    fun selectTab(tab: HomeNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Simula chegada de push / evento de backend: energia normalizada após queda.
     */
    fun onEnergyRestoredNotification(notification: EnergyRestoredNotification) {
        _uiState.update { s ->
            val card = EnergyHistoryUiItem.EnergyRestoredCard(notification)
            val withoutOldRestored = s.energyHistory.filterIsInstance<EnergyHistoryUiItem.RecordedOutage>()
            s.copy(
                energyHistory = listOf(card) + withoutOldRestored,
            )
        }
    }

    fun dismissFirstRestoredCard() {
        _uiState.update { s ->
            when (s.energyHistory.firstOrNull()) {
                is EnergyHistoryUiItem.EnergyRestoredCard -> s.copy(energyHistory = s.energyHistory.drop(1))
                else -> s
            }
        }
    }

    companion object {
        const val DEFAULT_DEVICE_ID = "demo-device"
    }
}
