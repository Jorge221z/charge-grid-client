package com.jorge.chargegridapp.station

import com.jorge.chargegridapp.station.network.dto.StationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch


// Possibles states of our UI
data class StationUiState(
    val isLoading: Boolean = false,
    val stations: List<StationResponse> = emptyList(),
    val errorMessage: String? = null,

    val stationDetail: StationDetailResponse? = null,
    val isFetchingDetail: Boolean = false,
    val stationCreatedSuccessfully: Boolean = false
)


class StationViewModel(private val repository: StationRepository): ViewModel() {
    // Tell the StateFlow to hold StationUiState, and initialize it with the default values
    private val _uiState = MutableStateFlow(StationUiState()) // Modifier
    val uiState: StateFlow<StationUiState> = _uiState.asStateFlow() // Reader on the UI

    init {
        // Fetch data immediately when the ViewModel is created
        fetchAllStations()
    }

    private fun fetchAllStations() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.fetchAllStations()

            result.onSuccess { stations ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        stations = stations,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }

    fun fetchStationDetail(id: Long) {
        _uiState.value = _uiState.value.copy(isFetchingDetail = true)

        viewModelScope.launch {
            val result = repository.fetchStationDetail(id)

            result.onSuccess { detail ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isFetchingDetail = false,
                        stationDetail = detail,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isFetchingDetail = false,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }

    fun createStation(request: StationCreateRequest) {
        viewModelScope.launch {
            val result = repository.createStation(request)

            result.onSuccess { newStation ->
                val updatedStations = _uiState.value.stations + newStation
                _uiState.update { currentState ->
                    currentState.copy(
                        stationCreatedSuccessfully = true,
                        stations = updatedStations,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }

    fun resetStationCreatedFlag() {
        _uiState.value = _uiState.value.copy(stationCreatedSuccessfully = false)
    }

    fun clearStationDetail() {
        _uiState.value = _uiState.value.copy(stationDetail = null)
    }

    fun updateStationStatus(id: Long, request: StationStatusUpdateRequest) {
        viewModelScope.launch {
            val result = repository.updateStationStatus(id, request)

            result.onSuccess { updatedStation ->
                val updatedStations = _uiState.value.stations.map { station ->
                    if (station.id == id) updatedStation else station
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        stations = updatedStations,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }

}
