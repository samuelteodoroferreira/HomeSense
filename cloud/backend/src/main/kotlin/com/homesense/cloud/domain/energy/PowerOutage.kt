package com.homesense.cloud.domain.energy

import java.time.Instant
import java.util.UUID

/**
 * Queda de energia detectada: início quando a tensão permanece em zero além do limiar temporal.
 */
data class PowerOutage(
    val id: UUID = UUID.randomUUID(),
    val deviceId: String,
    /** Primeiro instante em que a tensão foi considerada zero (início da interrupção). */
    val startedAt: Instant,
)
