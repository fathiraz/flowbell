package com.fathiraz.flowbell.core.utils

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import timber.log.Timber

/**
 * Manager for debug tools like Hyperion and Chucker
 * Provides centralized control over debug tool visibility and functionality
 */
object DebugToolsManager {

    private var chuckerCollector: ChuckerCollector? = null
    private var isDebugModeEnabled = false

    /**
     * Initialize debug tools - called from Application.onCreate()
     */
    fun initialize(context: Context) {
        try {
            // Initialize Chucker collector (but don't show notification initially)
            chuckerCollector = ChuckerCollector(
                context = context,
                showNotification = false, // Start with notification disabled
                retentionPeriod = com.chuckerteam.chucker.api.RetentionManager.Period.ONE_WEEK
            )

            LoggerUtils.App.d("Debug tools initialized (disabled by default)")
        } catch (e: Exception) {
            LoggerUtils.App.e("Failed to initialize debug tools", e)
        }
    }

    /**
     * Enable or disable debug tools based on user preference
     */
    fun setDebugModeEnabled(context: Context, enabled: Boolean) {
        isDebugModeEnabled = enabled

        try {
            if (enabled) {
                enableDebugTools(context)
            } else {
                disableDebugTools(context)
            }

            LoggerUtils.App.d("Debug mode ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            LoggerUtils.App.e("Failed to update debug tools state", e)
        }
    }

    /**
     * Get current debug mode state
     */
    fun isDebugModeEnabled(): Boolean = isDebugModeEnabled

    /**
     * Get Chucker collector instance
     */
    fun getChuckerCollector(): ChuckerCollector? = chuckerCollector

    private fun enableDebugTools(context: Context) {
        // Enable Hyperion (only available in debug builds)
        try {
            // Use reflection to avoid compile-time dependency in release builds
            val hyperionClass = Class.forName("com.willowtreeapps.hyperion.core.Hyperion")
            val enableMethod = hyperionClass.getMethod("enable")
            enableMethod.invoke(null)
            LoggerUtils.App.d("Hyperion enabled")
        } catch (e: Exception) {
            LoggerUtils.App.d("Hyperion not available (release build or missing dependency)")
        }

        // Enable Chucker notification
        try {
            // Note: ChuckerCollector doesn't have a direct way to enable/disable notification
            // after creation, so we recreate it with notification enabled
            chuckerCollector = ChuckerCollector(
                context = context,
                showNotification = true,
                retentionPeriod = com.chuckerteam.chucker.api.RetentionManager.Period.ONE_WEEK
            )
            LoggerUtils.App.d("Chucker notification enabled")
        } catch (e: Exception) {
            LoggerUtils.App.e("Failed to enable Chucker notification", e)
        }
    }

    private fun disableDebugTools(context: Context) {
        // Note: Hyperion doesn't have a direct disable method once enabled
        // The best we can do is not open it and hide UI elements
        LoggerUtils.App.d("Hyperion disabled (UI hidden)")

        // Disable Chucker notification by recreating collector
        try {
            chuckerCollector = ChuckerCollector(
                context = context,
                showNotification = false,
                retentionPeriod = com.chuckerteam.chucker.api.RetentionManager.Period.ONE_WEEK
            )
            LoggerUtils.App.d("Chucker notification disabled")
        } catch (e: Exception) {
            LoggerUtils.App.e("Failed to disable Chucker notification", e)
        }
    }
}