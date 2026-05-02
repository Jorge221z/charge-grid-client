package com.jorge.chargegridapp.ui.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.station.network.dto.Status

@Composable
fun GlobalMapContent(
    stations: List<StationResponse>,
    onStationClick: (Long) -> Unit
) {
    var isEuropeOnly by remember { mutableStateOf(true) }
    
    // Europe approximate bounds
    val europeMinLat = 34.0
    val europeMaxLat = 72.0
    val europeMinLon = -25.0
    val europeMaxLon = 45.0

    val filteredStations = if (isEuropeOnly) {
        stations.filter { 
            it.latitude in europeMinLat..europeMaxLat && 
            it.longitude in europeMinLon..europeMaxLon 
        }
    } else {
        stations
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4168, -3.7038), 4f)
    }

    // Dynamic Zoom: Fit selected stations in view
    LaunchedEffect(filteredStations, isEuropeOnly) {
        if (filteredStations.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            filteredStations.forEach { builder.include(LatLng(it.latitude, it.longitude)) }
            val bounds = builder.build()
            
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 150),
                durationMs = 1000
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            stations.forEach { station ->
                val isIncluded = !isEuropeOnly || (
                    station.latitude in europeMinLat..europeMaxLat && 
                    station.longitude in europeMinLon..europeMaxLon
                )

                val statusHue = when (station.status) {
                    Status.AVAILABLE -> BitmapDescriptorFactory.HUE_GREEN
                    Status.MAINTENANCE -> 35f // More vibrant orange matching WarningOrange
                    else -> BitmapDescriptorFactory.HUE_RED
                }

                Marker(
                    state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                    title = station.name,
                    snippet = "Power: ${station.maxPower.toInt()} kW. Tap for details.",
                    icon = BitmapDescriptorFactory.defaultMarker(statusHue),
                    alpha = if (isIncluded) 1.0f else 0.35f,
                    onInfoWindowClick = {
                        onStationClick(station.id)
                    }
                )
            }
        }
        
        // Floating UI Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Region Toggle
            Surface(
                onClick = { isEuropeOnly = !isEuropeOnly },
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                shape = CircleShape,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEuropeOnly) "EUROPE FOCUS" else "GLOBAL VIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Station count indicator
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = "${filteredStations.size} OF ${stations.size} STATIONS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
