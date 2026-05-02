package com.homesense.fog

data class FogConfig(
    val mqttHost: String = System.getenv("HOMESENSE_MQTT_HOST") ?: "127.0.0.1",
    val mqttPort: Int = System.getenv("HOMESENSE_MQTT_PORT")?.toIntOrNull() ?: 1883,
    val subscribeTopicFilter: String =
        System.getenv("HOMESENSE_MQTT_SUBSCRIBE") ?: "homesense/edge/#",
    val publishTopicPrefix: String =
        System.getenv("HOMESENSE_FOG_TOPIC_PREFIX") ?: "homesense/fog",
    val httpPort: Int = System.getenv("HOMESENSE_FOG_HTTP_PORT")?.toIntOrNull() ?: 8080,
)
