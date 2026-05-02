package com.homesense.fog.domain.usecase

import com.homesense.fog.domain.model.EnvironmentReading
import com.homesense.fog.domain.model.PowerReading
import com.homesense.fog.domain.model.SensorQuality
import com.homesense.fog.domain.repository.ProcessedTelemetrySink

/**
 * Caso de uso: validação + pré-processamento na camada fog (SOLID: depende de abstração).
 */
class NormalizeAndForwardTelemetryUseCase(
    private val sink: ProcessedTelemetrySink,
) {
    suspend fun handleEnvironment(edgeDeviceId: String, temperatureC: Double, humidityPercent: Double, ts: Long) {
        val quality = when {
            temperatureC < -50 || temperatureC > 60 -> SensorQuality.INVALID
            humidityPercent < 0 || humidityPercent > 100 -> SensorQuality.INVALID
            else -> SensorQuality.OK
        }
        val normalized = EnvironmentReading(
            edgeDeviceId = edgeDeviceId,
            temperatureC = temperatureC.coerceIn(-40.0, 55.0),
            humidityPercent = humidityPercent.coerceIn(0.0, 100.0),
            timestampEpochSeconds = ts,
            sensorQuality = quality,
        )
        sink.publishEnvironment(normalized)
    }

    suspend fun handlePower(edgeDeviceId: String, voltageRms: Double, ts: Long) {
        val clamped = voltageRms.coerceIn(0.0, 400.0)
        sink.publishPower(PowerReading(edgeDeviceId, clamped, ts))
    }
}
