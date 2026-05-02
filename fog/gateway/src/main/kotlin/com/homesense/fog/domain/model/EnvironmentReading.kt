package com.homesense.fog.domain.model

data class EnvironmentReading(
    val edgeDeviceId: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val timestampEpochSeconds: Long,
    val sensorQuality: SensorQuality,
)

enum class SensorQuality {
    OK,
    INVALID,
}
