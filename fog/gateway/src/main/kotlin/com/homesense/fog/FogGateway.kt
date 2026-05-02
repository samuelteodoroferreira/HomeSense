package com.homesense.fog

import com.homesense.fog.di.fogAppModule
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    val config = FogConfig()
    embeddedServer(Netty, port = config.httpPort) {
        install(Koin) {
            slf4jLogger()
            modules(fogAppModule(config))
        }
        module()
    }.start(wait = true)
}

fun Application.module() {
    val mqttBootstrap: FogMqttBootstrap by inject()
    mqttBootstrap.start()
    environment.monitor.subscribe(ApplicationStopped) {
        mqttBootstrap.stop()
    }
    routing {
        route("/health", HttpMethod.Get) {
            handle {
                context.respondText(
                    """{"service":"HOME_SENSE_FOG","status":"UP"}""",
                    ContentType.Application.Json,
                )
            }
        }
    }
}
