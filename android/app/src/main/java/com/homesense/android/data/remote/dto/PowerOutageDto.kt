package com.homesense.android.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PowerOutageDto(
    val deviceId: String,
    val startedAt: String,
)
