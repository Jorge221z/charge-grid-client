package com.jorge.chargegridapp.core.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jorge.chargegridapp.R
import okhttp3.Interceptor
import okhttp3.Response

class NetworkDebugInterceptor(private val context: Context) : Interceptor {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "network_debug_push"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Network Debug (Heads-up)"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Shows real-time API request popups"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!DebugConfig.isHeadsUpEnabled(context)) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val url = request.url.toString().substringAfterLast("/")
        val method = request.method

        // Show start notification
        showNotification(100, "🚀 API Request: $method", "/$url")

        val response: Response
        val startTime = System.currentTimeMillis()
        
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            showNotification(101, "❌ API Error", e.message ?: "Unknown Connection Error")
            throw e
        }

        val duration = System.currentTimeMillis() - startTime
        val code = response.code
        val icon = if (response.isSuccessful) "✅" else "⚠️"

        // Show finish notification
        showNotification(100, "$icon API Response: $code", "/$url (${duration}ms)")

        return response
    }

    private fun showNotification(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setTimeoutAfter(3000) // Disappear after 3 seconds
            .build()

        notificationManager.notify(id, notification)
    }
}
