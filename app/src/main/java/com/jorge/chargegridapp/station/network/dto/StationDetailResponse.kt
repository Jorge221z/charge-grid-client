package com.jorge.chargegridapp.station.network.dto

import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionSummaryResponse
import kotlinx.serialization.Serializable

@Serializable
data class StationDetailResponse(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val maxPower: Double,
    val status: Status,
    val recentSessions: List<ChargeSessionSummaryResponse>
)
