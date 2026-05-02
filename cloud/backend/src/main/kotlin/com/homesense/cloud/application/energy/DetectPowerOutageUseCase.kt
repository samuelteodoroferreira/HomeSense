package com.homesense.cloud.application.energy

import com.homesense.cloud.config.EnergyMonitoringProperties
import com.homesense.cloud.domain.energy.PowerOutage
import com.homesense.cloud.domain.energy.PowerOutageRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Monitora leituras de tensão e registra o início de uma queda quando RMS ≤ [zeroMaxVolts]
 * de forma contínua por pelo menos [outageConfirmSeconds].
 */
@Service
class DetectPowerOutageUseCase(
    private val outageRepository: PowerOutageRepository,
    private val properties: EnergyMonitoringProperties,
) {
    private data class MonitorState(
        var zeroSince: Instant? = null,
        var outageRecordedForEpisode: Boolean = false,
    )

    private val stateByDevice = ConcurrentHashMap<String, MonitorState>()

    fun onVoltageSample(deviceId: String, voltageRms: Double, observedAt: Instant) {
        val st = stateByDevice.computeIfAbsent(deviceId) { MonitorState() }
        synchronized(st) {
            if (voltageRms > properties.zeroMaxVolts) {
                st.zeroSince = null
                st.outageRecordedForEpisode = false
                return
            }
            if (st.zeroSince == null) {
                st.zeroSince = observedAt
            }
            if (st.outageRecordedForEpisode) {
                return
            }
            val zeroStart = st.zeroSince ?: return
            val elapsed = Duration.between(zeroStart, observedAt)
            if (elapsed >= Duration.ofSeconds(properties.outageConfirmSeconds)) {
                outageRepository.save(
                    PowerOutage(
                        deviceId = deviceId,
                        startedAt = zeroStart,
                    ),
                )
                st.outageRecordedForEpisode = true
            }
        }
    }
}
