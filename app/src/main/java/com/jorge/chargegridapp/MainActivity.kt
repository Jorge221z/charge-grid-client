package com.jorge.chargegridapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.chargegridapp.core.network.RetrofitClient
import com.jorge.chargegridapp.station.StationRepository
import com.jorge.chargegridapp.station.StationViewModel
import com.jorge.chargegridapp.station.ui.StationListScreen
import com.jorge.chargegridapp.ui.theme.ChargeGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeGridTheme {

                val factory = object: ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val api = RetrofitClient.stationApi
                        val repository = StationRepository(api)
                        @Suppress("UNCHECKED_CAST")
                        return StationViewModel(repository) as T
                    }
                }

                StationListScreen(
                    viewModel = viewModel(factory = factory)
                )
            }
        }
    }
}
