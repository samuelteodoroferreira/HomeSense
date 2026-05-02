package com.homesense.fog.domain.usecase

import com.homesense.fog.domain.model.PowerReading
import com.homesense.fog.domain.model.SensorQuality
import com.homesense.fog.domain.repository.ProcessedTelemetrySink
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NormalizeAndForwardTelemetryUseCaseTest {

    private val sink = mockk<ProcessedTelemetrySink>(relaxed = true)

    @Test
    fun `ambiente válido marca OK e encaminha`() = runTest {
        val uc = NormalizeAndForwardTelemetryUseCase(sink)
        val slot = slot<com.homesense.fog.domain.model.EnvironmentReading>()
        uc.handleEnvironment("ABC", 22.5, 55.0, 1000L)
        coVerify(exactly = 1) { sink.publishEnvironment(capture(slot)) }
        assertEquals(SensorQuality.OK, slot.captured.sensorQuality)
        assertEquals("ABC", slot.captured.edgeDeviceId)
    }

    @Test
    fun `temperatura fora da faixa marca INVALID`() = runTest {
        val uc = NormalizeAndForwardTelemetryUseCase(sink)
        val slot = slot<com.homesense.fog.domain.model.EnvironmentReading>()
        uc.handleEnvironment("ABC", 200.0, 50.0, 1000L)
        coVerify(exactly = 1) { sink.publishEnvironment(capture(slot)) }
        assertEquals(SensorQuality.INVALID, slot.captured.sensorQuality)
    }

    @Test
    fun `tensão é limitada ao máximo configurado no use case`() = runTest {
        val uc = NormalizeAndForwardTelemetryUseCase(sink)
        val slot = slot<PowerReading>()
        uc.handlePower("ABC", 9999.0, 2000L)
        coVerify(exactly = 1) { sink.publishPower(capture(slot)) }
        assertEquals(400.0, slot.captured.voltageRms, 0.001)
    }
}
