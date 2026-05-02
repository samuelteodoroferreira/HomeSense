package com.homesense.fog.data.mqtt

import com.homesense.fog.domain.usecase.NormalizeAndForwardTelemetryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory

class MqttFogIngress(
    private val useCase: NormalizeAndForwardTelemetryUseCase,
    private val topicParser: MqttTopicParser = MqttTopicParser,
    private val payloadParser: EdgePayloadParser = EdgePayloadParser(),
) : MqttCallback {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun connectionLost(cause: Throwable?) {
        log.warn("MQTT desconectado: {}", cause?.message)
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        val id = topicParser.edgeDeviceIdFromTopic(topic) ?: return
        val raw = String(message.payload, Charsets.UTF_8)
        scope.launch {
            when {
                topicParser.isEnvironmentTopic(topic) ->
                    payloadParser.parseEnv(raw).onSuccess { p ->
                        useCase.handleEnvironment(id, p.t, p.h, p.timestampEpochSeconds)
                    }.onFailure { log.debug("Payload env inválido: {}", raw) }

                topicParser.isPowerTopic(topic) ->
                    payloadParser.parsePower(raw).onSuccess { p ->
                        useCase.handlePower(id, p.voltageRms, p.timestampEpochSeconds)
                    }.onFailure { log.debug("Payload power inválido: {}", raw) }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
}
