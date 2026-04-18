package com.jorge.chargegridapp.station

import com.jorge.chargegridapp.station.network.StationApi
import com.jorge.chargegridapp.station.network.dto.StationResponse


// The UI will call these methods to get the data
class StationRepository(private val stationApi: StationApi) {

    suspend fun fetchAllStations(): List<StationResponse> {
        return runCatching {
            stationApi.getAllStations()
        }.onFailure { exception ->
            println("Error fetching stations: ${exception.message}")
        }.getOrDefault(emptyList()) // Return an empty list if there is an error
    }
}