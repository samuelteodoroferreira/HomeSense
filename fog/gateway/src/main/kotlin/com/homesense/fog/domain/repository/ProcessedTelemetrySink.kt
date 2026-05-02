package com.homesense.fog.domain.repository

import com.homesense.fog.domain.model.EnvironmentReading
import com.homesense.fog.domain.model.PowerReading

/**
 * Porta de saída: envia telemetria já normalizada (ex.: republish MQTT, HTTP para cloud).
 */
interface ProcessedTelemetrySink {
    suspend fun publishEnvironment(reading: EnvironmentReading)
    suspend fun publishPower(reading: PowerReading)
}
