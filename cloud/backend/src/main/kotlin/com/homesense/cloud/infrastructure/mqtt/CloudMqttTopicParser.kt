package com.homesense.cloud.infrastructure.mqtt

object CloudMqttTopicParser {
    fun deviceId(topic: String): String? {
        val parts = topic.trim('/').split('/')
        if (parts.size < 4) return null
        if (parts[0] != "homesense" || parts[1] != "fog") return null
        return parts[2].takeIf { it.isNotBlank() }
    }

    fun isEnv(topic: String): Boolean = topic.endsWith("/env")
    fun isPower(topic: String): Boolean = topic.endsWith("/power")
}
