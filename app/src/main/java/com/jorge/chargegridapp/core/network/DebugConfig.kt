package com.jorge.chargegridapp.core.network

import android.content.Context
import com.jorge.chargegridapp.BuildConfig

object DebugConfig {
    private const val PREFS_NAME = "debug_prefs"
    private const val KEY_BASE_URL = "api_base_url"
    private const val KEY_HEADS_UP = "heads_up_notifications"
    
    // Default fallback to BuildConfig value or standard localhost
    private val DEFAULT_URL = BuildConfig.API_BASE_URL.removeSurrounding("\"")

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun setBaseUrl(context: Context, newUrl: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, newUrl).apply()
    }

    fun isHeadsUpEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HEADS_UP, false)
    }

    fun setHeadsUpEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HEADS_UP, enabled).apply()
    }
    
    fun resetToDefault(context: Context) {
        setBaseUrl(context, DEFAULT_URL)
        setHeadsUpEnabled(context, false)
    }
}
