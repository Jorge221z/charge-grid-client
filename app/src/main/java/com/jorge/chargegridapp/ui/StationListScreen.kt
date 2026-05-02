package com.jorge.chargegridapp.ui

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.jorge.chargegridapp.core.ui.components.ChargeGridButton
import androidx.compose.material.icons.filled.Warning
import com.jorge.chargegridapp.core.ui.screens.theme.*

@Composable
fun StationScreen(
    viewModel: StationViewModel = viewModel(),
    sessionViewModel: ChargeSessionViewModel = viewModel()
) {
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

@Composable
fun StationListContent(
    stations: List<StationResponse>,
    isLoading: Boolean,
    errorMessage: String? = null,
    onStationClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    onCreateSubmit: (StationCreateRequest) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ChargeGridButton(
                text = "New Station",
                onClick = { showDialog = true },
                modifier = Modifier.width(160.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Stations",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(24.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && stations.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (errorMessage != null && stations.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryClick) {
                            Text("Retry")
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stations) { station ->
                        StationCard(
                            name = station.name,
                            status = station.status.name,
                            maxPower = station.maxPower,
                            onClick = { onStationClick(station.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateStationDialog(
            onSubmit = { request ->
                onCreateSubmit(request)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun StationCard(name: String, status: String, maxPower: Double, onClick: () -> Unit) {
    val statusColor = when (status.uppercase()) {
        "AVAILABLE" -> ElectricGreen
        "MAINTENANCE" -> WarningOrange
        else -> AlertRed
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = status.uppercase().replace("_", " "),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${maxPower.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "kW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    onStatusUpdate: (Status) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STATION POWER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${detail.maxPower.toInt()} kW",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val statusColor = when (detail.status) {
                            Status.AVAILABLE -> ElectricGreen
                            Status.MAINTENANCE -> WarningOrange
                            else -> AlertRed
                        }
                        
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = detail.status.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DetailInfoRow(Icons.Default.LocationOn, "Coordinates", "${detail.latitude}, ${detail.longitude}")
                    DetailInfoRow(Icons.Default.Info, "Station ID", "#${detail.id}")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "CHARGING CONTROL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            val inProgressSession = detail.recentSessions.find { it.endTime == null }
            val isCurrentStationSession = activeSession?.stationId == detail.id
            val isSessionActive = isCurrentStationSession || inProgressSession != null

            if (isSessionLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                var showMaintenanceError by remember { mutableStateOf(false) }

                AnimatedVisibility(
                    visible = !isSessionActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ChargeGridButton(
                        text = "Start Charging",
                        onClick = { 
                            if (detail.status == Status.MAINTENANCE) {
                                showMaintenanceError = true
                            } else {
                                onStartCharge(StartSessionRequest(stationId = detail.id)) 
                            }
                        }
                    )
                }

                if (showMaintenanceError) {
                    ModernErrorDialog(
                        title = "Station Unavailable",
                        message = "This station is currently under maintenance. Please set it to 'Available' before starting a session.",
                        onDismiss = { showMaintenanceError = false }
                    )
                }

                AnimatedVisibility(
                    visible = isSessionActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ActiveChargingCard(
                        startTime = if (isCurrentStationSession) activeSession?.startTime else inProgressSession?.startTime,
                        onStopClick = {
                            val idToStop = if (isCurrentStationSession) activeSession?.id else inProgressSession?.id
                            if (idToStop != null) onStopCharge(idToStop)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "QUICK STATUS UPDATE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusButton(Modifier.weight(1f), "Available", ElectricGreen) { onStatusUpdate(Status.AVAILABLE) }
                StatusButton(Modifier.weight(1f), "Maintenance", WarningOrange) { onStatusUpdate(Status.MAINTENANCE) }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECENT SESSIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (detail.recentSessions.isEmpty()) {
                Text(
                    text = "No charging history yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                detail.recentSessions.forEach { session ->
                    SessionHistoryCard(session)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StatusButton(modifier: Modifier, text: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ActiveChargingCard(startTime: java.time.LocalDateTime?, onStopClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeWidth = 2.dp
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "CHARGING ACTIVE",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            val formattedTime = remember(startTime) {
                startTime?.formatToDisplay() ?: ""
            }
            Text(
                text = "Started at $formattedTime",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            ChargeGridButton(
                text = "Stop Session",
                onClick = onStopClick,
                gradientColors = listOf(AlertRed, Color(0xFFB71C1C)),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun SessionHistoryCard(session: ChargeSessionSummaryResponse) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session #${session.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val statusText = if (session.endTime == null) "LIVE" else "COMPLETED"
                val statusBgColor = if (session.endTime == null) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                }
                val statusColor = if (session.endTime == null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }

                Surface(
                    color = statusBgColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(text = "ENERGY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${session.kwhConsumed} kWh", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    val durationLabel = if (session.endTime == null) "ELAPSED" else "DURATION"
                    Text(text = durationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    val durationValue = remember(session.startTime, session.endTime) {
                        com.jorge.chargegridapp.core.network.utils.calculateDuration(session.startTime, session.endTime)
                    }
                    Text(text = durationValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Started: ${session.startTime.formatToDisplay()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.AddLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Register New Station",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Station Name",
                    icon = Icons.Default.Badge,
                    error = nameError
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PremiumTextField(
                        value = latitude,
                        onValueChange = { latitude = it; latError = null },
                        label = "Latitude",
                        icon = Icons.Default.CompassCalibration,
                        error = latError,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                        labelStyle = MaterialTheme.typography.labelSmall
                    )
                    PremiumTextField(
                        value = longitude,
                        onValueChange = { longitude = it; lonError = null },
                        label = "Longitude",
                        icon = Icons.Default.CompassCalibration,
                        error = lonError,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                        labelStyle = MaterialTheme.typography.labelSmall
                    )
                }

                PremiumTextField(
                    value = maxPower,
                    onValueChange = { maxPower = it; powerError = null },
                    label = "Max Power (kW)",
                    icon = Icons.Default.EvStation,
                    error = powerError,
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            ChargeGridButton(
                text = "Register",
                onClick = {
                    var isValid = true
                    if (name.isBlank()) { nameError = "Required"; isValid = false }
                    val latNum = latitude.toDoubleOrNull()
                    if (latNum == null || latNum !in -90.0..90.0) { latError = "Invalid"; isValid = false }
                    val lonNum = longitude.toDoubleOrNull()
                    if (lonNum == null || lonNum !in -180.0..180.0) { lonError = "Invalid"; isValid = false }
                    val powerNum = maxPower.toDoubleOrNull()
                    if (powerNum == null || powerNum <= 0) { powerError = "Invalid"; isValid = false }

                    if (isValid) {
                        onSubmit(StationCreateRequest(name, latNum!!, lonNum!!, powerNum!!))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
fun ModernErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            ChargeGridButton(
                text = "Understood",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    )
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = labelStyle) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
