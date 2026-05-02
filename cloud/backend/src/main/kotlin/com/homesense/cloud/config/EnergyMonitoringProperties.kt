package com.homesense.cloud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "homesense.energy")
data class EnergyMonitoringProperties(
    /** Tensão RMS abaixo ou igual a este valor é tratada como "zero" (sem rede). */
    val zeroMaxVolts: Double = 10.0,
    /** Tempo contínuo em zero para confirmar e registrar o início da queda. */
    val outageConfirmSeconds: Long = 5L,
)
