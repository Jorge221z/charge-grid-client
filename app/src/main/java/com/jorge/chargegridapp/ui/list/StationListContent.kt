package com.jorge.chargegridapp.ui.list

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorge.chargegridapp.core.ui.components.ChargeGridButton
import com.jorge.chargegridapp.station.network.dto.StationCreateRequest
import com.jorge.chargegridapp.station.network.dto.StationResponse
import com.jorge.chargegridapp.ui.maps.GlobalMapContent

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
    var isMapView by remember { mutableStateOf(false) }

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
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stations",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                IconButton(
                    onClick = { isMapView = !isMapView },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                        contentDescription = "Toggle View"
                    )
                }
            }

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

                if (isMapView && stations.isNotEmpty()) {
                    GlobalMapContent(stations = stations, onStationClick = onStationClick)
                } else {
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
