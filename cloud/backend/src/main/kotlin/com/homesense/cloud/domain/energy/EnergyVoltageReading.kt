package com.homesense.cloud.domain.energy

import java.time.Instant
import java.util.UUID

/**
 * Amostra de tensão (ex.: ZMPT101B / RMS estimado) no domínio Energy.
 */
data class EnergyVoltageReading(
    val id: UUID = UUID.randomUUID(),
    val deviceId: String,
    val voltageRms: Double,
    val recordedAt: Instant,
)
