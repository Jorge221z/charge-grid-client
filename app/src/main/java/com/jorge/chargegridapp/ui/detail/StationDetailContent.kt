package com.jorge.chargegridapp.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.chargegridapp.chargesession.network.dto.ChargeSessionResponse
import com.jorge.chargegridapp.chargesession.network.dto.StartSessionRequest
import com.jorge.chargegridapp.core.ui.components.ChargeGridButton
import com.jorge.chargegridapp.core.ui.screens.theme.ElectricGreen
import com.jorge.chargegridapp.core.ui.screens.theme.WarningOrange
import com.jorge.chargegridapp.core.ui.screens.theme.AlertRed
import com.jorge.chargegridapp.station.network.dto.StationDetailResponse
import com.jorge.chargegridapp.station.network.dto.Status
import com.jorge.chargegridapp.ui.components.ModernErrorDialog

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
                                text = detail.status.name.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DetailInfoRow(Icons.Default.Info, "Station ID", "#${detail.id}")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Map
                    val stationLocation = LatLng(detail.latitude, detail.longitude)
                    val detailCameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(stationLocation, 12f)
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = detailCameraState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false
                            ),
                            properties = MapProperties(isMyLocationEnabled = false)
                        ) {
                            Marker(
                                state = MarkerState(position = stationLocation),
                                title = detail.name
                            )
                        }
                    }
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

            var showChargingError by remember { mutableStateOf(false) }

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
                StatusButton(Modifier.weight(1f), "Available", ElectricGreen) { 
                    if (isSessionActive) {
                        showChargingError = true
                    } else {
                        onStatusUpdate(Status.AVAILABLE) 
                    }
                }
                StatusButton(Modifier.weight(1f), "Maintenance", WarningOrange) { 
                    if (isSessionActive) {
                        showChargingError = true
                    } else {
                        onStatusUpdate(Status.MAINTENANCE) 
                    }
                }
            }

            if (showChargingError) {
                ModernErrorDialog(
                    title = "Action Restricted",
                    message = "You cannot change the station status while a charging session is active. Please stop the session first.",
                    onDismiss = { showChargingError = false }
                )
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
