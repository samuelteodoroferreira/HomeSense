package com.homesense.fog.data.mqtt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FogEnvOut(
    val t: Double,
    val h: Double,
    val ts: Long,
    val q: String,
)

@Serializable
data class FogPowerOut(
    @SerialName("v_rms") val voltageRms: Double,
    val ts: Long,
)
