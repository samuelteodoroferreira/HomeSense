package com.homesense.android.data.repository

import com.homesense.android.data.remote.HomesenseApi
import com.homesense.android.domain.model.DashboardSnapshot
import com.homesense.android.domain.repository.DashboardRepository
import java.time.Instant
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: HomesenseApi,
) : DashboardRepository {

    override suspend fun loadDashboard(deviceId: String): DashboardSnapshot {
        val climate = runCatching { api.climateReadings(deviceId) }.getOrNull().orEmpty()
        val latest = climate.maxWithOrNull(
            compareBy { dto ->
                runCatching { Instant.parse(dto.recordedAt) }.getOrElse { Instant.EPOCH }
            },
        )
        val outages = runCatching { api.powerOutages(deviceId) }.getOrNull().orEmpty()
        return DashboardSnapshot(
            temperatureC = latest?.temperatureC,
            humidityPercent = latest?.humidityPercent,
            climateRecordedAt = latest?.recordedAt,
            outageStartsIso = outages.map { it.startedAt },
        )
    }
}
