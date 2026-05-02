package com.homesense.cloud.domain.climate

fun interface ClimateReadingRepository {
    fun save(reading: ClimateReading): ClimateReading
}
