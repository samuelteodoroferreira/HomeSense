package com.homesense.android.presentation.home

import com.homesense.android.domain.model.EnergyRestoredNotification

enum class HomeNavTab {
    Climate,
    Energy,
    Security,
}

sealed class EnergyHistoryUiItem {
    data class EnergyRestoredCard(
        val outageDate: String,
        val outageTime: String,
        val durationText: String,
    ) : EnergyHistoryUiItem() {
        constructor(n: EnergyRestoredNotification) : this(n.outageDate, n.outageTime, n.durationText)
    }

    data class RecordedOutage(val startedAtLabel: String) : EnergyHistoryUiItem()
}

data class HomeDashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val temperatureC: Double? = null,
    val humidityPercent: Double? = null,
    val climateSubtitle: String? = null,
    val selectedTab: HomeNavTab = HomeNavTab.Climate,
    val energyHistory: List<EnergyHistoryUiItem> = emptyList(),
)
