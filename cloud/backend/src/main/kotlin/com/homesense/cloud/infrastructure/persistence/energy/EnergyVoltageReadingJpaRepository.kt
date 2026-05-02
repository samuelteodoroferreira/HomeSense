package com.homesense.cloud.infrastructure.persistence.energy

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EnergyVoltageReadingJpaRepository : JpaRepository<EnergyVoltageReadingEntity, UUID> {
    fun findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId: String): List<EnergyVoltageReadingEntity>
}
