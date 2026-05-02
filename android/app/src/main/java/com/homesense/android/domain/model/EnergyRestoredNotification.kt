package com.homesense.android.domain.model

/**
 * Payload de notificação (ex.: FCM ou simulação em teste) quando a energia volta após queda.
 */
data class EnergyRestoredNotification(
    val outageDate: String,
    val outageTime: String,
    val durationText: String,
)
