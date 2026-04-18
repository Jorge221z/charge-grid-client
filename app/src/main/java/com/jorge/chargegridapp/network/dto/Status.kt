package com.jorge.chargegridapp.network.dto

import kotlinx.serialization.Serializable

// This class replies the one from the API
@Serializable
enum class Status {
    AVAILABLE,
    IN_USE,
    MAINTENANCE
}