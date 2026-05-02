package com.homesense.fog.data.mqtt

import com.homesense.fog.domain.model.EnvironmentReading
import com.homesense.fog.domain.model.PowerReading
import com.homesense.fog.domain.repository.ProcessedTelemetrySink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.MqttClient
import org.slf4j.LoggerFactory

class MqttRepublishSink(
    private val client: MqttClient,
    private val topicPrefix: String,
    private val json: Json = Json { encodeDefaults = true },
) : ProcessedTelemetrySink {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun publishEnvironment(reading: EnvironmentReading) {
        val topic = "$topicPrefix/${reading.edgeDeviceId}/env"
        val body = json.encodeToString(
            FogEnvOut(
                t = reading.temperatureC,
                h = reading.humidityPercent,
                ts = reading.timestampEpochSeconds,
                q = reading.sensorQuality.name,
            ),
        )
        publish(topic, body)
    }

    override suspend fun publishPower(reading: PowerReading) {
        val topic = "$topicPrefix/${reading.edgeDeviceId}/power"
        val body = json.encodeToString(
            FogPowerOut(
                voltageRms = reading.voltageRms,
                ts = reading.timestampEpochSeconds,
            ),
        )
        publish(topic, body)
    }

    private suspend fun publish(topic: String, body: String) = withContext(Dispatchers.IO) {
        try {
            if (!client.isConnected) return@withContext
            client.publish(topic, body.toByteArray(Charsets.UTF_8), 1, false)
        } catch (e: Exception) {
            log.warn("Falha ao publicar MQTT {}: {}", topic, e.message)
        }
    }
}
