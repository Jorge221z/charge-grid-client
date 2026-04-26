package com.jorge.chargegridapp.core.network

import com.jorge.chargegridapp.BuildConfig
import com.jorge.chargegridapp.station.network.StationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    private const val BASE_URL = BuildConfig.API_BASE_URL

    // Network logger
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Level.BODY means it will print the headers AND the actual JSON payload to Logcat
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Attach the logging interceptor to OkHttpClient
    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Configure parser
    private val json = Json { ignoreUnknownKeys = true } // Avoid crashes if API adds new fields we don't know about

    // Core engine
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(
                "application/json".toMediaType())) // Connect Kotlinx Serialization
            .build()
    }

    // Expose API interfaces
    val stationApi: StationApi by lazy {
        retrofit.create(StationApi::class.java)
    }

}