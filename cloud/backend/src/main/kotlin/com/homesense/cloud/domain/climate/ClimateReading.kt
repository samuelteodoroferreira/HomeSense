package com.homesense.cloud.domain.climate

import java.time.Instant
import java.util.UUID

/**
 * Leitura de ambiente (ex.: DHT11) no domínio Climate.
 */
data class ClimateReading(
    val id: UUID = UUID.randomUUID(),
    val deviceId: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val recordedAt: Instant,
    val sensorQuality: String? = null,
)
