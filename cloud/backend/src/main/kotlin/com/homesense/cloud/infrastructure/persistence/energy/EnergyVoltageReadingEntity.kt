package com.homesense.cloud.infrastructure.persistence.energy

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "energy_voltage_readings")
class EnergyVoltageReadingEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val deviceId: String,
    @Column(nullable = false)
    val voltageRms: Double,
    @Column(nullable = false)
    val recordedAt: Instant,
)
