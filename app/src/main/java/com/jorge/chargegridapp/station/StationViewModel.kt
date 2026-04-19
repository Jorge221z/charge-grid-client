package com.jorge.chargegridapp.station

import com.jorge.chargegridapp.station.network.dto.StationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch


// Possibles states of our UI
data class StationUiState(
    val isLoading: Boolean = false,
    val stations: List<StationResponse> = emptyList(),
    val errorMessage: String? = null
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    stations = stations,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }

}
