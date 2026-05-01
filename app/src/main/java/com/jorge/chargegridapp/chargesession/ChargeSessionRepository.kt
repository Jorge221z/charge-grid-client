package com.jorge.chargegridapp.chargesession

import com.jorge.chargegridapp.chargesession.network.ChargeSessionApi
import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest

class ChargeSessionRepository(private val chargeSessionApi: ChargeSessionApi) {

    suspend fun startSession(request: StartSessionRequest): Result<ChargeSessionResponse> {
        return runCatching {
            chargeSessionApi.startSession(request)
        }
    }

    suspend fun stopSession(id: Long): Result<ChargeSessionResponse> {
        return runCatching {
            chargeSessionApi.stopSession(id)
        }
    }

}