package com.homesense.cloud.adapter.web

import com.homesense.cloud.infrastructure.persistence.energy.EnergyVoltageReadingEntity
import com.homesense.cloud.infrastructure.persistence.energy.EnergyVoltageReadingJpaRepository
import com.homesense.cloud.infrastructure.persistence.energy.PowerOutageEntity
import com.homesense.cloud.infrastructure.persistence.energy.PowerOutageJpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/energy")
class EnergyReadingController(
    private val voltageJpa: EnergyVoltageReadingJpaRepository,
    private val outageJpa: PowerOutageJpaRepository,
) {

    @GetMapping("/{deviceId}/voltage")
    fun voltageHistory(@PathVariable deviceId: String): List<EnergyVoltageReadingDto> =
        voltageJpa.findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId).map { it.toDto() }

    @GetMapping("/{deviceId}/outages")
    fun outages(@PathVariable deviceId: String): List<PowerOutageDto> =
        outageJpa.findTop50ByDeviceIdOrderByStartedAtDesc(deviceId).map { it.toDto() }
}

data class EnergyVoltageReadingDto(
    val deviceId: String,
    val voltageRms: Double,
    val recordedAt: String,
)

data class PowerOutageDto(
    val deviceId: String,
    val startedAt: String,
)

private fun EnergyVoltageReadingEntity.toDto() = EnergyVoltageReadingDto(
    deviceId = deviceId,
    voltageRms = voltageRms,
    recordedAt = recordedAt.toString(),
)

private fun PowerOutageEntity.toDto() = PowerOutageDto(
    deviceId = deviceId,
    startedAt = startedAt.toString(),
)
