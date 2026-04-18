package com.jorge.chargegridapp.chargesession.network.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime


// No stationId since sessions already have a Station object
@Serializable
data class ChargeSessionSummaryResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val kwhConsumed: Double,
)
