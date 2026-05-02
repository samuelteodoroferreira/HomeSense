package com.homesense.cloud.application.energy

import com.homesense.cloud.domain.energy.EnergyVoltageReading
import com.homesense.cloud.domain.energy.EnergyVoltageReadingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RecordEnergyVoltageReadingUseCase(
    private val energyVoltageReadingRepository: EnergyVoltageReadingRepository,
    private val detectPowerOutageUseCase: DetectPowerOutageUseCase,
) {
    @Transactional
    fun record(deviceId: String, voltageRms: Double, tsEpochSeconds: Long) {
        val at = Instant.ofEpochSecond(tsEpochSeconds)
        energyVoltageReadingRepository.save(
            EnergyVoltageReading(
                deviceId = deviceId,
                voltageRms = voltageRms,
                recordedAt = at,
            ),
        )
        detectPowerOutageUseCase.onVoltageSample(deviceId, voltageRms, at)
    }
}
