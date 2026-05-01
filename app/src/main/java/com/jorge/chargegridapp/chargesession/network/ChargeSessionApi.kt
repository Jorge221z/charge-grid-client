package com.jorge.chargegridapp.chargesession.network

import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path


private const val BASE_URL = "api/sessions"

interface ChargeSessionApi {

    @POST("$BASE_URL/start")
    suspend fun startSession(@Body request: StartSessionRequest): ChargeSessionResponse

    @POST("$BASE_URL/{id}/stop")
    suspend fun stopSession(@Path("id") id: Long): ChargeSessionResponse
}