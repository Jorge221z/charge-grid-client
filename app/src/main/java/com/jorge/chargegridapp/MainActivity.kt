package com.jorge.chargegridapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.chargesession.ChargeSessionViewModel
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.ui.StationScreen
import com.jorge.chargegridapp.core.ui.screens.theme.ChargeGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeGridTheme {
                StationScreen(
                    viewModel = viewModel(factory = StationViewModel.Factory),
                    sessionViewModel = viewModel(factory = ChargeSessionViewModel.Factory)
                )
            }
        }
    }
}
