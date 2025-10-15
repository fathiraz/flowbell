package com.fathiraz.flowbell.core.utils

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import timber.log.Timber

/**
 * Manager for debug tools (Chucker HTTP inspector and Beagle debug menu)
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
        // Beagle initializes automatically - no manual enable needed

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
        // Beagle handles its own lifecycle - no manual disable needed

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