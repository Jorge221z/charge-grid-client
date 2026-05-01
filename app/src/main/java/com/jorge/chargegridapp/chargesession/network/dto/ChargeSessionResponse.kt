package com.jorge.chargegridapp.chargesession.network.dto

import com.jorge.chargegridapp.station.network.dto.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ChargeSessionResponse(
    val id: Long,
    val stationId: Long,

    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime,

    @Serializable(with = LocalDateTimeSerializer::class)
    val endTime: LocalDateTime?,

    val kwhConsumed: Double
)
