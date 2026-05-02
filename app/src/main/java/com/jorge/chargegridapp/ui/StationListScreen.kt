package com.jorge.chargegridapp.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
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
import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionSummaryResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.station.network.dto.StationStatusUpdateRequest
import com.jorge.chargegridapp.station.network.dto.Status
import com.jorge.chargegridapp.core.network.utils.formatToDisplay

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

    LaunchedEffect(sessionState.activeSession) {
        state.stationDetail?.let { detail ->
            viewModel.fetchStationDetail(detail.id)
        }
    }

    // Refresh station detail after a session stops properly (loading finishes)
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
                val request = StationStatusUpdateRequest(status = newStatus)

                viewModel.updateStationStatus(
                    id = state.stationDetail!!.id,
                    request = request
                )
            },
            onRefreshStation = { viewModel.fetchStationDetail(state.stationDetail!!.id) }
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
    onStopCharge: (Long) -> Unit,
    onBackClick: () -> Unit,
    onStatusUpdate: (Status) -> Unit,
    onRefreshStation: (() -> Unit)? = null
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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

            val inProgressSession = detail.recentSessions.find { it.endTime == null }
            val isSessionActive = activeSession != null || inProgressSession != null

            if (isSessionLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (!isSessionActive) {
                Button(
                    onClick = { onStartCharge(StartSessionRequest(stationId = detail.id)) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Start Charging Session")
                }
            } else {
                Button(
                    onClick = {
                        val idToStop = activeSession?.id ?: inProgressSession?.id
                        if (idToStop != null) {
                            onStopCharge(idToStop)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Stop Charging Session")
                }

                // Avoid unnecessary CPU cycles
                val startTime = activeSession?.startTime ?: inProgressSession?.startTime
                val formattedTime = remember(startTime) {
                    startTime?.formatToDisplay() ?: ""
                }
                Text("Session Started At: $formattedTime", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "Recent Sessions", style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (detail.recentSessions.isEmpty()) {
                Text("No charging history yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                // Loop through the sessions (Just like a .map() in React!)
                detail.recentSessions.forEach { session ->
                    SessionHistoryCard(session)
                }
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
fun SessionHistoryCard(session: ChargeSessionSummaryResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Session ID: ${session.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Start: ${session.startTime.formatToDisplay()}", style = MaterialTheme.typography.bodyMedium)
            if (session.endTime != null) {
                Text(text = "End: ${session.endTime.formatToDisplay()}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(text = "In Progress", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Energy Consumed: ${session.kwhConsumed} kWh", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CreateStationDialog(onSubmit: (StationCreateRequest) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var maxPower by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var latError by remember { mutableStateOf<String?>(null) }
    var lonError by remember { mutableStateOf<String?>(null) }
    var powerError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Station") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Station Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = latitude,
                    onValueChange = { latitude = it; latError = null },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = latError != null,
                    supportingText = { latError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = longitude,
                    onValueChange = { longitude = it; lonError = null },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = lonError != null,
                    supportingText = { lonError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = maxPower,
                    onValueChange = { maxPower = it; powerError = null },
                    label = { Text("Max Power (kW)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = powerError != null,
                    supportingText = { powerError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                var isValid = true

                if (name.isBlank()) {
                    nameError = "Name cannot be empty"
                    isValid = false
                }

                val latNum = latitude.toDoubleOrNull()
                if (latNum == null || latNum !in -90.0..90.0) {
                    latError = "Valid latitude is between -90 and 90"
                    isValid = false
                }

                val lonNum = longitude.toDoubleOrNull()
                if (lonNum == null || lonNum !in -180.0..180.0) {
                    lonError = "Valid longitude is between -180 and 180"
                    isValid = false
                }

                val powerNum = maxPower.toDoubleOrNull()
                if (powerNum == null || powerNum <= 0) {
                    powerError = "Power must be greater than 0"
                    isValid = false
                }

                if (isValid) {
                    val request = StationCreateRequest(
                        name = name,
                        latitude = latNum!!,
                        longitude = lonNum!!,
                        maxPower = powerNum!!
                    )
                    onSubmit(request)
                }
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