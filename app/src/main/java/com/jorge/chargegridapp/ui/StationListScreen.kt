package com.jorge.chargegridapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.chargesession.ChargeSessionViewModel
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest
import com.jorge.chargegridapp.ui.detail.StationDetailContent
import com.jorge.chargegridapp.ui.list.StationListContent

@Composable
fun StationScreen(
    viewModel: StationViewModel = viewModel(),
    sessionViewModel: ChargeSessionViewModel = viewModel()
) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        ChargeGridSplashScreen(onSplashFinished = { showSplash = false })
    } else {
        val state by viewModel.uiState.collectAsState()
        val sessionState by sessionViewModel.uiState.collectAsState()

        BackHandler(enabled = state.stationDetail != null) {
            viewModel.clearStationDetail()
        }

        LaunchedEffect(sessionState.activeSession) {
            state.stationDetail?.let { detail ->
                viewModel.fetchStationDetail(detail.id)
            }
        }

        LaunchedEffect(sessionState.isLoading) {
            if (!sessionState.isLoading && sessionState.errorMessage == null) {
                state.stationDetail?.let { detail ->
                    viewModel.fetchStationDetail(detail.id)
                }
            }
        }

        if (state.stationDetail == null) {
            StationListContent(
                stations = state.stations,
                isLoading = state.isLoading || state.isFetchingDetail,
                errorMessage = state.errorMessage,
                onStationClick = { id -> viewModel.fetchStationDetail(id) },
                onRetryClick = { viewModel.fetchAllStations() },
                onCreateSubmit = { request -> viewModel.createStation(request) }
            )
        } else {
            StationDetailContent(
                detail = state.stationDetail!!,
                activeSession = sessionState.activeSession,
                isSessionLoading = sessionState.isLoading,
                onStartCharge = { request -> sessionViewModel.startSession(request) },
                onStopCharge = { sessionId -> sessionViewModel.stopSession(sessionId) },
                onBackClick = { viewModel.clearStationDetail() },
                onStatusUpdate = { newStatus ->
                    viewModel.updateStationStatus(
                        id = state.stationDetail!!.id,
                        request = StationStatusUpdateRequest(status = newStatus)
                    )
                }
            )
        }
    }
}
