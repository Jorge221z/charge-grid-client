package com.jorge.chargegridapp.chargesession.network.dto

import kotlinx.serialization.Serializable


@Serializable
data class StartSessionRequest(
// ID of the station requested to be used
    val stationId: Long,
)
