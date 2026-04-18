package com.jorge.chargegridapp.station.network.dto

import kotlinx.serialization.Serializable


/**
 * Fields allowed by the backend via HTTP requests
 */
@Serializable
data class StationCreateRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val maxPower: Double,
)

