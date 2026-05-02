package com.homesense.cloud.application.climate

import com.homesense.cloud.domain.climate.ClimateReading
import com.homesense.cloud.domain.climate.ClimateReadingRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RecordClimateReadingUseCase(
    private val climateReadingRepository: ClimateReadingRepository,
) {
    fun record(deviceId: String, temperatureC: Double, humidityPercent: Double, tsEpochSeconds: Long, quality: String?) {
        climateReadingRepository.save(
            ClimateReading(
                deviceId = deviceId,
                temperatureC = temperatureC,
                humidityPercent = humidityPercent,
                recordedAt = Instant.ofEpochSecond(tsEpochSeconds),
                sensorQuality = quality,
            ),
        )
    }
}
