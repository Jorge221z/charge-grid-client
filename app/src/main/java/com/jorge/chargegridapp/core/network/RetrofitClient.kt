package com.jorge.chargegridapp.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jorge.chargegridapp.BuildConfig
import com.jorge.chargegridapp.chargesession.network.ChargeSessionApi
import com.jorge.chargegridapp.station.network.StationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber

object RetrofitClient {

    private var retrofitInstance: Retrofit? = null
    private var okHttpClientInstance: OkHttpClient? = null

    // Configure parser
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Rebuilds the Retrofit instance with current DebugConfig and Chucker.
     */
    fun initialize(context: Context) {
        val baseUrl = DebugConfig.getBaseUrl(context)
        Timber.d("Initializing Retrofit with URL: $baseUrl")

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .collector(
                ChuckerCollector(
                    context = context,
                    showNotification = true
                )
            )
            .maxContentLength(250000L)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(true)
            .build()

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(NetworkDebugInterceptor(context))
            .build()

        okHttpClientInstance = client

        retrofitInstance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private fun getRetrofit(context: Context? = null): Retrofit {
        if (retrofitInstance == null) {
            // Safety fallback if someone calls API before initialize
            // Ideally we should have a way to get global context here
            // We'll throw an error or handle it gracefully
            throw IllegalStateException("RetrofitClient must be initialized with a Context first.")
        }
        return retrofitInstance!!
    }

    // Dynamic API getters
    val stationApi: StationApi
        get() = retrofitInstance?.create(StationApi::class.java) 
            ?: throw IllegalStateException("Retrofit not initialized")

    val chargeSessionApi: ChargeSessionApi
        get() = retrofitInstance?.create(ChargeSessionApi::class.java)
            ?: throw IllegalStateException("Retrofit not initialized")

}