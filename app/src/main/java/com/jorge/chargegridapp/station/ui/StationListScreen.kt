package com.jorge.chargegridapp.station.ui

import androidx.compose.material3.AlertDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.chargesession.ChargeSessionViewModel
import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest
import com.jorge.chargegridapp.station.network.dto.Status

@Composable
fun StationScreen(
    viewModel: StationViewModel = viewModel(),
    sessionViewModel: ChargeSessionViewModel = viewModel()
) {
    // Observers
    val state by viewModel.uiState.collectAsState()
    val sessionState by sessionViewModel.uiState.collectAsState()

    // Interceptor for physical Android back button
    BackHandler(enabled = state.stationDetail != null) {
        viewModel.clearStationDetail()
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
            onStopCharge = { sessionViewModel.stopSession() },

            onBackClick = { viewModel.clearStationDetail() },
            onStatusUpdate = { newStatus ->
                val request = StationStatusUpdateRequest(status = newStatus)

                viewModel.updateStationStatus(
                    id = state.stationDetail!!.id,
                    request = request
                )
            }
        )
    }
}

@Composable
fun StationListContent(
    stations: List<StationResponse>,
    isLoading: Boolean,
    errorMessage: String? = null,
    onStationClick: (Long) -> Unit, // Callback
    onRetryClick: () -> Unit, // Retry callback in case of error
    onCreateSubmit: (StationCreateRequest) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
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
            } // closes LazyColumn
            } // closes Box
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            ) {
                Text("Create New Station")
            }
        } // closes Column
    } // closes Scaffold

    if (showDialog) {
        CreateStationDialog(
            onSubmit = { request ->
                onCreateSubmit(request)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@Composable
fun StationDetailContent(
    detail: StationDetailResponse,
    activeSession: ChargeSessionResponse?,
    isSessionLoading: Boolean,
    onStartCharge: (StartSessionRequest) -> Unit,
    onStopCharge: () -> Unit,
    onBackClick: () -> Unit,
    onStatusUpdate: (Status) -> Unit
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
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            DetailRow(label = "ID", value = detail.id.toString())
            DetailRow(label = "Name", value = detail.name)
            DetailRow(label = "Status", value = detail.status.name)
            DetailRow(label = "Max Power", value = "${detail.maxPower} kW")
            DetailRow(label = "Coordinates", value = "${detail.latitude}, ${detail.longitude}")

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Update Status", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onStatusUpdate(Status.AVAILABLE) }) {
                    Text("Available")
                }
                Button(onClick = { onStatusUpdate(Status.IN_USE) }) {
                    Text("In Use")
                }
                Button(onClick = { onStatusUpdate(Status.MAINTENANCE) }) {
                    Text("Under Maintenance")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSessionLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (activeSession == null) {
                Button(
                    onClick = { onStartCharge(StartSessionRequest(stationId = detail.id)) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Start Charging Session")
                }
            } else {
                Button(
                    onClick = onStopCharge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Stop Charging Session")
                }

                Text("Session Started At: ${activeSession.startTime}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp), // Same as Spacer
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
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

@Composable
fun CreateStationDialog(onSubmit: (StationCreateRequest) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var maxPower by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Station") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Station Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = maxPower,
                    onValueChange = { maxPower = it },
                    label = { Text("Max Power (kW)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val request = StationCreateRequest(
                    name = name,
                    latitude = latitude.toDoubleOrNull() ?: 0.0,
                    longitude = longitude.toDoubleOrNull() ?: 0.0,
                    maxPower = maxPower.toDoubleOrNull() ?: 0.0,
                )
                onSubmit(request)
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}