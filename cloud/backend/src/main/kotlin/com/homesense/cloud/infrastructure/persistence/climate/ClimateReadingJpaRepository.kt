package com.homesense.cloud.infrastructure.persistence.climate

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClimateReadingJpaRepository : JpaRepository<ClimateReadingEntity, UUID> {
    fun findTop50ByDeviceIdOrderByRecordedAtDesc(deviceId: String): List<ClimateReadingEntity>
}
