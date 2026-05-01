package com.jorge.chargegridapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.ui.StationScreen
import com.jorge.chargegridapp.ui.theme.ChargeGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeGridTheme {
                StationScreen(
                    viewModel = viewModel(factory = StationViewModel.Factory)
                )
            }
        }
    }
}
