package com.homesense.cloud.domain.energy

fun interface PowerOutageRepository {
    fun save(outage: PowerOutage): PowerOutage
}
