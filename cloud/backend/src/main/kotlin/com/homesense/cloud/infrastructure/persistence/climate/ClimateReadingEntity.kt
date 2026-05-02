package com.homesense.cloud.infrastructure.persistence.climate

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "climate_readings")
class ClimateReadingEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val deviceId: String,
    @Column(nullable = false)
    val temperatureC: Double,
    @Column(nullable = false)
    val humidityPercent: Double,
    @Column(nullable = false)
    val recordedAt: Instant,
    val sensorQuality: String? = null,
)
