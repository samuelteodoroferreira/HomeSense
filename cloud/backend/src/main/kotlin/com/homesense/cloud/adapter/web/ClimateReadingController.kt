package com.homesense.cloud.adapter.web

import com.homesense.cloud.infrastructure.persistence.climate.ClimateReadingEntity
import com.homesense.cloud.infrastructure.persistence.climate.ClimateReadingJpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/climate")
class ClimateReadingController(
    private val jpa: ClimateReadingJpaRepository,
) {

    @GetMapping("/{deviceId}")
    fun latestByDevice(@PathVariable deviceId: String): List<ClimateReadingDto> =
        jpa.findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId).map { it.toDto() }
}

data class ClimateReadingDto(
    val deviceId: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val recordedAt: String,
    val sensorQuality: String?,
)

private fun ClimateReadingEntity.toDto() = ClimateReadingDto(
    deviceId = deviceId,
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    recordedAt = recordedAt.toString(),
    sensorQuality = sensorQuality,
)
