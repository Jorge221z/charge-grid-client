package com.jorge.chargegridapp.station.network

import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "/api/stations"

interface StationApi {

    @GET(BASE_URL)
    suspend fun getAllStations(): List<StationResponse>

    @GET("$BASE_URL/{id}")
    suspend fun getStationDetail(@Path("id") id: Long): StationDetailResponse

    @POST(BASE_URL)
    suspend fun createStation(@Body request: StationCreateRequest): StationResponse

    @PATCH("$BASE_URL/{id}/status")
    suspend fun updateStationStatus(@Path("id") id: Long, @Body request: StationStatusUpdateRequest): StationResponse

}