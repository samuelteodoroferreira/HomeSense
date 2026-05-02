package com.homesense.android.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClimateReadingDto(
    val deviceId: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val recordedAt: String,
    val sensorQuality: String?,
)
