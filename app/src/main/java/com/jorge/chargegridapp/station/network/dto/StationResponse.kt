package com.jorge.chargegridapp.station.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StationResponse(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val maxPower: Double,
    val status: Status
)