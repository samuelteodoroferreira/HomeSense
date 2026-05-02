package com.homesense.fog.domain.model

data class PowerReading(
    val edgeDeviceId: String,
    val voltageRms: Double,
    val timestampEpochSeconds: Long,
)
