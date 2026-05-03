package com.jorge.chargegridapp.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.api.Chucker
import com.jorge.chargegridapp.core.network.DebugConfig
import com.jorge.chargegridapp.core.network.RetrofitClient
import com.jorge.chargegridapp.core.ui.components.ChargeGridButton

@Composable
fun DeveloperMenu(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(DebugConfig.getBaseUrl(context)) }
    var headsUpEnabled by remember { mutableStateOf(DebugConfig.isHeadsUpEnabled(context)) }

    // Request notification permission for Android 13+ so Chucker notifications show up
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled by system */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("Developer Mode", style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("API Base URL", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server Address") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text("Network Debugging", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                ChargeGridButton(
                    text = "Open Chucker History",
                    onClick = {
                        val intent = Chucker.getLaunchIntent(context)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Push Notifications", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Real-time API popups",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = headsUpEnabled,
                        onCheckedChange = { 
                            headsUpEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            ChargeGridButton(
                text = "Apply & Reload",
                onClick = {
                    DebugConfig.setBaseUrl(context, url)
                    DebugConfig.setHeadsUpEnabled(context, headsUpEnabled)
                    // Re-initialize Retrofit with new URL and config
                    RetrofitClient.initialize(context)
                    onDismiss()
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
