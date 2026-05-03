package com.jorge.chargegridapp

import android.app.Application
import com.jorge.chargegridapp.core.network.RetrofitClient
import timber.log.Timber

class ChargeGridApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize network client with context for Chucker
        RetrofitClient.initialize(this)
        
        // Global Exception Handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "FATAL CRASH in thread ${thread.name}")
            // Re-throw or let the system handle it
        }
        
        // Initialize Timber for intelligent logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("ChargeGridApp initialized in DEBUG mode")
        }
    }
}
