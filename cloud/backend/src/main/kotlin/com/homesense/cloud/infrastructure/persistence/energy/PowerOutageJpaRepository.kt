package com.homesense.cloud.infrastructure.persistence.energy

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PowerOutageJpaRepository : JpaRepository<PowerOutageEntity, UUID> {
    fun findTop50ByDeviceIdOrderByStartedAtDesc(deviceId: String): List<PowerOutageEntity>
}
