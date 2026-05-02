package com.homesense.cloud.infrastructure.persistence.climate

import com.homesense.cloud.domain.climate.ClimateReading
import com.homesense.cloud.domain.climate.ClimateReadingRepository
import org.springframework.stereotype.Component

@Component
class ClimateReadingPersistenceAdapter(
    private val jpa: ClimateReadingJpaRepository,
) : ClimateReadingRepository {

    override fun save(reading: ClimateReading): ClimateReading {
        jpa.save(
            ClimateReadingEntity(
                id = reading.id,
                deviceId = reading.deviceId,
                temperatureC = reading.temperatureC,
                humidityPercent = reading.humidityPercent,
                recordedAt = reading.recordedAt,
                sensorQuality = reading.sensorQuality,
            ),
        )
        return reading
    }
}
