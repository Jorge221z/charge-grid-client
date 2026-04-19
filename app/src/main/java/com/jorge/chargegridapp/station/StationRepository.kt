package com.jorge.chargegridapp.station

import com.jorge.chargegridapp.station.network.StationApi
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest


// The UI will call these methods to get the data
class StationRepository(private val stationApi: StationApi) {

    suspend fun fetchAllStations(): Result<List<StationResponse>> {
        return runCatching {
            stationApi.getAllStations()
        }
    }

    suspend fun fetchStationDetail(id: Long): Result<StationDetailResponse> {
        return runCatching {
            stationApi.getStationDetail(id)
        }
    }

    suspend fun createStation(request: StationCreateRequest): Result<StationResponse> {
        return runCatching {
            stationApi.createStation(request)
        }
    }

    suspend fun updateStationStatus(id: Long, request: StationStatusUpdateRequest): Result<StationResponse> {
        return runCatching {
            stationApi.updateStationStatus(id, request)
        }
    }
}