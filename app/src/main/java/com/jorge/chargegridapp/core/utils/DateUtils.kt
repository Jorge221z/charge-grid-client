package com.jorge.chargegridapp.core.network.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatToDisplay(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return this.format(formatter)
}