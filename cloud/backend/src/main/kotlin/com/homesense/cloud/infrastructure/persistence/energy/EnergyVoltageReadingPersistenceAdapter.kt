package com.homesense.cloud.infrastructure.persistence.energy

import com.homesense.cloud.domain.energy.EnergyVoltageReading
import com.homesense.cloud.domain.energy.EnergyVoltageReadingRepository
import org.springframework.stereotype.Component

@Component
class EnergyVoltageReadingPersistenceAdapter(
    private val jpa: EnergyVoltageReadingJpaRepository,
) : EnergyVoltageReadingRepository {

    override fun save(reading: EnergyVoltageReading): EnergyVoltageReading {
        jpa.save(
            EnergyVoltageReadingEntity(
                id = reading.id,
                deviceId = reading.deviceId,
                voltageRms = reading.voltageRms,
                recordedAt = reading.recordedAt,
            ),
        )
        return reading
    }
}
