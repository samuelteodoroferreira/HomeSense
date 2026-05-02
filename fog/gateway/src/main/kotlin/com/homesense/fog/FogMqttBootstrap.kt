package com.homesense.fog

import com.homesense.fog.data.mqtt.MqttFogIngress
import org.eclipse.paho.client.mqttv3.MqttClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class FogMqttBootstrap : KoinComponent {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client: MqttClient by inject()
    private val ingress: MqttFogIngress by inject()
    private val config: FogConfig by inject()

    fun start() {
        client.setCallback(ingress)
        client.connect()
        client.subscribe(config.subscribeTopicFilter, 1)
        log.info("Fog MQTT ligado em {} — assinatura {}", config.mqttHost, config.subscribeTopicFilter)
    }

    fun stop() {
        runCatching { client.disconnect() }
    }
}
