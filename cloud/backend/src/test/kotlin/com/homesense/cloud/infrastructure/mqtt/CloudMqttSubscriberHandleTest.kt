package com.homesense.cloud.infrastructure.mqtt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.homesense.cloud.application.climate.RecordClimateReadingUseCase
import com.homesense.cloud.application.energy.RecordEnergyVoltageReadingUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CloudMqttSubscriberHandleTest {

    @Test
    fun `parse env chama caso de uso de climate`() {
        val climate = mockk<RecordClimateReadingUseCase>(relaxed = true)
        val energy = mockk<RecordEnergyVoltageReadingUseCase>(relaxed = true)
        val sub = CloudMqttSubscriber(
            recordClimateReadingUseCase = climate,
            recordEnergyVoltageReadingUseCase = energy,
            objectMapper = jacksonObjectMapper(),
            host = "127.0.0.1",
            port = 1883,
            clientId = "homesense-test",
            subscribeTopic = "homesense/fog/#",
        )
        sub.handle("homesense/fog/ABC123/env", """{"t":22,"h":50,"ts":100,"q":"OK"}""")
        verify(exactly = 1) { climate.record("ABC123", 22.0, 50.0, 100L, "OK") }
        verify(exactly = 0) { energy.record(any(), any(), any()) }
    }

    @Test
    fun `parse power chama caso de uso de energy`() {
        val climate = mockk<RecordClimateReadingUseCase>(relaxed = true)
        val energy = mockk<RecordEnergyVoltageReadingUseCase>(relaxed = true)
        val sub = CloudMqttSubscriber(
            recordClimateReadingUseCase = climate,
            recordEnergyVoltageReadingUseCase = energy,
            objectMapper = jacksonObjectMapper(),
            host = "127.0.0.1",
            port = 1883,
            clientId = "homesense-test",
            subscribeTopic = "homesense/fog/#",
        )
        sub.handle("homesense/fog/ABC123/power", """{"v_rms":127,"ts":200}""")
        verify(exactly = 1) { energy.record("ABC123", 127.0, 200L) }
        verify(exactly = 0) { climate.record(any(), any(), any(), any(), any()) }
    }
}
