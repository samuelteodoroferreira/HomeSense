package com.homesense.cloud.domain.energy

fun interface EnergyVoltageReadingRepository {
    fun save(reading: EnergyVoltageReading): EnergyVoltageReading
}
