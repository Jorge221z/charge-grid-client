package com.jorge.chargegridapp.station.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationResponse

@Composable
fun StationScreen(
    viewModel: StationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.stationDetail == null) {
        StationListContent(
            stations = state.stations,
            isLoading = state.isLoading || state.isFetchingDetail,
            onStationClick = { id -> viewModel.fetchStationDetail(id) }
        )
    } else {
        StationDetailContent(
            detail = state.stationDetail!!,
            onBackClick = { viewModel.clearStationDetail() }
        )
    }
}

@Composable
fun StationListContent(
    stations: List<StationResponse>,
    isLoading: Boolean,
    onStationClick: (Long) -> Unit // Callback
) {
    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp)
            ) {
                items(stations) { station ->
                    StationCard(
                        name = station.name,
                        status = station.status.name,
                        onClick = { onStationClick(station.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StationDetailContent(
    detail: StationDetailResponse,
    onBackClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = onBackClick) {
                Text("⬅ Go Back")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Station Details", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ID: ${detail.id}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Name: ${detail.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Status: ${detail.status.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Max Power: ${detail.maxPower}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Latitude: ${detail.latitude}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Longitude: ${detail.longitude}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Charge Sessions Nº: ${detail.recentSessions.count()}", style = MaterialTheme.typography.bodyLarge)

        }
    }
}

@Composable
fun StationCard(name: String, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Status: $status", style = MaterialTheme.typography.bodyMedium)
        }
    }
}