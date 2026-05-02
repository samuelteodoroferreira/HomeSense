package com.homesense.fog.data.mqtt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MqttTopicParserTest {

    @Test
    fun extraiIdDoDispositivo() {
        assertEquals("A1B2", MqttTopicParser.edgeDeviceIdFromTopic("homesense/edge/A1B2/env"))
    }

    @Test
    fun topicoInvalidoRetornaNulo() {
        assertNull(MqttTopicParser.edgeDeviceIdFromTopic("other/topic"))
    }
}
