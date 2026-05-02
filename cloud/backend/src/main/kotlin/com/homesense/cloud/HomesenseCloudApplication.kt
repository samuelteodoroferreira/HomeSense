package com.homesense.cloud

import com.homesense.cloud.config.EnergyMonitoringProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan("com.homesense.cloud.infrastructure.persistence")
@EnableJpaRepositories("com.homesense.cloud.infrastructure.persistence")
@EnableConfigurationProperties(EnergyMonitoringProperties::class)
class HomesenseCloudApplication

fun main(args: Array<String>) {
    runApplication<HomesenseCloudApplication>(*args)
}
