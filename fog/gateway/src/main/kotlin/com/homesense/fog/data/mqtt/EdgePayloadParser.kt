package com.homesense.fog.data.mqtt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EnvPayload(
    val t: Double,
    val h: Double,
    @SerialName("ts") val timestampEpochSeconds: Long,
)

@Serializable
data class PowerPayload(
    @SerialName("v_rms") val voltageRms: Double,
    @SerialName("ts") val timestampEpochSeconds: Long,
)

class EdgePayloadParser(private val json: Json = Json { ignoreUnknownKeys = true }) {
    fun parseEnv(raw: String): Result<EnvPayload> = runCatching { json.decodeFromString<EnvPayload>(raw) }
    fun parsePower(raw: String): Result<PowerPayload> = runCatching { json.decodeFromString<PowerPayload>(raw) }
}
