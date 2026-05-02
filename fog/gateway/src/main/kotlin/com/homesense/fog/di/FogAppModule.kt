package com.homesense.fog.di

import com.homesense.fog.FogConfig
import com.homesense.fog.FogMqttBootstrap
import com.homesense.fog.data.mqtt.MqttFogIngress
import com.homesense.fog.data.mqtt.MqttRepublishSink
import com.homesense.fog.domain.repository.ProcessedTelemetrySink
import com.homesense.fog.domain.usecase.NormalizeAndForwardTelemetryUseCase
import org.eclipse.paho.client.mqttv3.MqttClient
import org.koin.dsl.module

fun fogAppModule(config: FogConfig) = module {
    single { config }
    single<MqttClient> {
        MqttClient(
            "tcp://${config.mqttHost}:${config.mqttPort}",
            "homesense-fog-${System.currentTimeMillis()}",
            null,
        )
    }
    single<ProcessedTelemetrySink> {
        MqttRepublishSink(get(), config.publishTopicPrefix)
    }
    single { NormalizeAndForwardTelemetryUseCase(get()) }
    single { MqttFogIngress(get()) }
    single { FogMqttBootstrap() }
}
