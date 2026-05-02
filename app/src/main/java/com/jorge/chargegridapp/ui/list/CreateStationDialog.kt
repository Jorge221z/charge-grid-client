package com.jorge.chargegridapp.ui.list

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.chargegridapp.core.ui.components.ChargeGridButton
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.ui.components.PremiumTextField

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

    // Maps State
    val defaultLocation = LatLng(40.4168, -3.7038) // Madrid
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
    }
    
    // Update map when manual coordinates change
    LaunchedEffect(latitude, longitude) {
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lon), cameraPositionState.position.zoom)
        }
    }

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
                // Map Selection
                Text(
                    "Tap to select location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            latitude = latLng.latitude.toString()
                            longitude = latLng.longitude.toString()
                        },
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        val lat = latitude.toDoubleOrNull()
                        val lon = longitude.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lon)),
                                title = "New Station"
                            )
                        }
                    }
                }

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
