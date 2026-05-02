package com.homesense.cloud.infrastructure.mqtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.homesense.cloud.application.climate.RecordClimateReadingUseCase
import com.homesense.cloud.application.energy.RecordEnergyVoltageReadingUseCase
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "homesense.mqtt", name = ["enabled"], havingValue = "true")
class CloudMqttSubscriber(
    private val recordClimateReadingUseCase: RecordClimateReadingUseCase,
    private val recordEnergyVoltageReadingUseCase: RecordEnergyVoltageReadingUseCase,
    private val objectMapper: ObjectMapper,
    @Value("\${homesense.mqtt.host}") private val host: String,
    @Value("\${homesense.mqtt.port}") private val port: Int,
    @Value("\${homesense.mqtt.client-id}") private val clientId: String,
    @Value("\${homesense.mqtt.subscribe-topic}") private val subscribeTopic: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var client: MqttClient

    @PostConstruct
    fun start() {
        val serverUri = "tcp://$host:$port"
        client = MqttClient(serverUri, clientId, null)
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            connectionTimeout = 10
            keepAliveInterval = 60
            isCleanSession = true
        }
        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                try {
                    client.subscribe(subscribeTopic, 1)
                    log.info("Cloud MQTT inscrito em {} (reconexão={})", subscribeTopic, reconnect)
                } catch (e: Exception) {
                    log.error("Falha ao subscrever MQTT após conectar", e)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                log.warn("MQTT cloud desconectado: {}", cause?.message)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                handle(topic, String(message.payload, Charsets.UTF_8))
            }

            override fun deliveryComplete(token: org.eclipse.paho.client.mqttv3.IMqttDeliveryToken?) = Unit
        })
        client.connect(options)
    }

    @PreDestroy
    fun stop() {
        runCatching { client.disconnect() }
    }

    fun handle(topic: String, raw: String) {
        val id = CloudMqttTopicParser.deviceId(topic) ?: return
        try {
            when {
                CloudMqttTopicParser.isEnv(topic) -> {
                    val p: Map<String, Any?> = objectMapper.readValue(raw)
                    val t = (p["t"] as Number).toDouble()
                    val h = (p["h"] as Number).toDouble()
                    val ts = (p["ts"] as Number).toLong()
                    val q = p["q"] as? String
                    recordClimateReadingUseCase.record(id, t, h, ts, q)
                }
                CloudMqttTopicParser.isPower(topic) -> {
                    val p: Map<String, Any?> = objectMapper.readValue(raw)
                    val v = (p["v_rms"] as Number).toDouble()
                    val ts = (p["ts"] as Number).toLong()
                    recordEnergyVoltageReadingUseCase.record(id, v, ts)
                }
            }
        } catch (e: Exception) {
            log.debug("Ignorando payload inválido em {}: {}", topic, e.message)
        }
    }
}
