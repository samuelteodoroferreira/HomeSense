package com.homesense.android.domain.model

data class DashboardSnapshot(
    val temperatureC: Double?,
    val humidityPercent: Double?,
    val climateRecordedAt: String?,
    val outageStartsIso: List<String>,
)
