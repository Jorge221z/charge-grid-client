package com.jorge.chargegridapp.station.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StationStatusUpdateRequest(
    val status: Status
)