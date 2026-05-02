package com.homesense.android.data.remote

import com.homesense.android.data.remote.dto.ClimateReadingDto
import com.homesense.android.data.remote.dto.PowerOutageDto
import retrofit2.http.GET
import retrofit2.http.Path

interface HomesenseApi {
    @GET("api/v1/climate/{deviceId}")
    suspend fun climateReadings(@Path("deviceId") deviceId: String): List<ClimateReadingDto>

    @GET("api/v1/energy/{deviceId}/outages")
    suspend fun powerOutages(@Path("deviceId") deviceId: String): List<PowerOutageDto>
}
