package com.homesense.cloud.application.energy

import com.homesense.cloud.config.EnergyMonitoringProperties
import com.homesense.cloud.domain.energy.PowerOutage
import com.homesense.cloud.domain.energy.PowerOutageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class DetectPowerOutageUseCaseTest {

    private val props = EnergyMonitoringProperties(
        zeroMaxVolts = 10.0,
        outageConfirmSeconds = 5L,
    )

    @Test
    fun `nao registra queda se tensao voltar antes de 5 segundos em zero`() {
        val repo = mockk<PowerOutageRepository>(relaxed = true)
        val uc = DetectPowerOutageUseCase(repo, props)
        val t0 = Instant.parse("2024-01-01T00:00:00Z")
        uc.onVoltageSample("dev", 0.0, t0)
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(4))
        uc.onVoltageSample("dev", 220.0, t0.plusSeconds(4))
        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun `registra inicio da queda no primeiro instante em que a tensao ficou zero por pelo menos 5s`() {
        val repo = mockk<PowerOutageRepository>()
        val slot = slot<PowerOutage>()
        every { repo.save(capture(slot)) } answers { firstArg() }
        val uc = DetectPowerOutageUseCase(repo, props)
        val t0 = Instant.parse("2024-01-01T00:00:00Z")
        uc.onVoltageSample("dev", 0.0, t0)
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(5))
        verify(exactly = 1) { repo.save(any()) }
        assertEquals(t0, slot.captured.startedAt)
        assertEquals("dev", slot.captured.deviceId)
    }

    @Test
    fun `nao duplica registro no mesmo episodio de queda`() {
        val repo = mockk<PowerOutageRepository>(relaxed = true)
        val uc = DetectPowerOutageUseCase(repo, props)
        val t0 = Instant.parse("2024-01-01T00:00:00Z")
        uc.onVoltageSample("dev", 0.0, t0)
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(5))
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(20))
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `apos retorno de tensao novo episodio zero gera novo registro`() {
        val repo = mockk<PowerOutageRepository>(relaxed = true)
        val uc = DetectPowerOutageUseCase(repo, props)
        val t0 = Instant.parse("2024-01-01T00:00:00Z")
        uc.onVoltageSample("dev", 0.0, t0)
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(5))
        uc.onVoltageSample("dev", 220.0, t0.plusSeconds(30))
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(40))
        uc.onVoltageSample("dev", 0.0, t0.plusSeconds(45))
        verify(exactly = 2) { repo.save(any()) }
    }

    @Test
    fun `tensao exatamente no limiar maximo de zero e tratada como ausencia de rede`() {
        val repo = mockk<PowerOutageRepository>(relaxed = true)
        val uc = DetectPowerOutageUseCase(repo, props)
        val t0 = Instant.parse("2024-01-01T00:00:00Z")
        uc.onVoltageSample("dev", 10.0, t0)
        uc.onVoltageSample("dev", 10.0, t0.plusSeconds(5))
        verify(exactly = 1) { repo.save(any()) }
    }
}
