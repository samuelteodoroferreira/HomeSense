package com.homesense.fog.data.mqtt

/**
 * Extrai device id de tópicos no formato homesense/edge/{id}/env|power
 */
object MqttTopicParser {
    fun edgeDeviceIdFromTopic(topic: String): String? {
        val parts = topic.trim('/').split('/')
        if (parts.size < 4) return null
        if (parts[0] != "homesense" || parts[1] != "edge") return null
        return parts[2].takeIf { it.isNotBlank() }
    }

    fun isEnvironmentTopic(topic: String): Boolean = topic.endsWith("/env")
    fun isPowerTopic(topic: String): Boolean = topic.endsWith("/power")
}
