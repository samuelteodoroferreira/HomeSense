package com.homesense.cloud.infrastructure.persistence.energy

import com.homesense.cloud.domain.energy.PowerOutage
import com.homesense.cloud.domain.energy.PowerOutageRepository
import org.springframework.stereotype.Component

@Component
class PowerOutagePersistenceAdapter(
    private val jpa: PowerOutageJpaRepository,
) : PowerOutageRepository {

    override fun save(outage: PowerOutage): PowerOutage {
        jpa.save(
            PowerOutageEntity(
                id = outage.id,
                deviceId = outage.deviceId,
                startedAt = outage.startedAt,
            ),
        )
        return outage
    }
}
