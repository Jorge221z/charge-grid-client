package com.jorge.chargegridapp.chargesession.network.dto

import com.jorge.chargegridapp.station.network.dto.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime


// No stationId since sessions already have a Station object
@Serializable
data class ChargeSessionSummaryResponse(
    val id: Long,

    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime,

    @Serializable(with = LocalDateTimeSerializer::class)
    val endTime: LocalDateTime?,

    val kwhConsumed: Double,
)
