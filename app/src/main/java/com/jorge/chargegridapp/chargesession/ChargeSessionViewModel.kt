package com.jorge.chargegridapp.chargesession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest
import com.jorge.chargegridapp.core.network.RetrofitClient
import com.jorge.chargegridapp.station.StationRepository
import com.jorge.chargegridapp.station.StationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ChargeSessionUiState(
    val activeSession: ChargeSessionResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChargeSessionViewModel(private val repository: ChargeSessionRepository): ViewModel() {

    private val _uiState = MutableStateFlow(ChargeSessionUiState())
    val uiState: StateFlow<ChargeSessionUiState> = _uiState.asStateFlow()

    fun startSession(request: StartSessionRequest) {
        _uiState.update { _uiState.value.copy(isLoading = true) }

        viewModelScope.launch {
            val result = repository.startSession(request)

            result.onSuccess { session ->
                _uiState.value = _uiState.value.copy(
                    activeSession = session,
                    isLoading = false,
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

    // No param needed since we will stop the active session
    fun stopSession() {
        //Check if the session is currently active
        val sessionId = _uiState.value.activeSession?.id ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.stopSession(sessionId)

            result.onSuccess { session ->
                _uiState.value = _uiState.value.copy(
                    activeSession = null,
                    isLoading = false,
                    errorMessage = null
                )
            // Our backend will manage the case that the session is already stopped or inactive
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = RetrofitClient.chargeSessionApi
                val repository = ChargeSessionRepository(api)
                return ChargeSessionViewModel(repository) as T
            }
        }
    }

}