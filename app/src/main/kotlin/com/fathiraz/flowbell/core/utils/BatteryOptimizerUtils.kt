package com.fathiraz.flowbell.core.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import timber.log.Timber

/**
 * Battery optimization utility for efficient resource management
 * Monitors device battery state and adjusts processing intensity accordingly
 */
class BatteryOptimizerUtils(private val context: Context) {

    companion object {
        // Battery level thresholds for different optimization levels (less restrictive)
        private const val CRITICAL_BATTERY_LEVEL = 10 // Below 10% - aggressive optimization
        private const val LOW_BATTERY_LEVEL = 20 // Below 20% - moderate optimization
        private const val MEDIUM_BATTERY_LEVEL = 35 // Below 35% - light optimization

        // Processing interval multipliers based on battery state (1-5 seconds range for near real-time)
        private const val NORMAL_PROCESSING_MULTIPLIER = 0.33f // 5 seconds
        private const val LIGHT_OPTIMIZATION_MULTIPLIER = 0.27f // 4 seconds
        private const val MODERATE_OPTIMIZATION_MULTIPLIER = 0.2f // 3 seconds
        private const val AGGRESSIVE_OPTIMIZATION_MULTIPLIER = 0.13f // 2 seconds

        // Batch size adjustments based on battery state
        private const val NORMAL_BATCH_SIZE = 50
        private const val OPTIMIZED_BATCH_SIZE = 25
        private const val AGGRESSIVE_BATCH_SIZE = 10
    }

    private val powerManager: PowerManager? = context.getSystemService()
    private val batteryManager: BatteryManager? = context.getSystemService()

    /**
     * Battery optimization level based on current device state
     */
    enum class OptimizationLevel {
        NORMAL,         // Battery > 50% or charging
        LIGHT,          // Battery 30-50%
        MODERATE,       // Battery 15-30%
        AGGRESSIVE      // Battery < 15% or battery saver mode
    }

    /**
     * Battery state information
     */
    data class BatteryState(
        val level: Int,
        val isCharging: Boolean,
        val isLowBattery: Boolean,
        val isBatterySaverOn: Boolean,
        val isDeviceIdleMode: Boolean,
        val optimizationLevel: OptimizationLevel,
        val processingMultiplier: Float,
        val recommendedBatchSize: Int
    )

    /**
     * Get current battery state and optimization recommendations
     */
    fun getCurrentBatteryState(): BatteryState {
        val batteryLevel = getBatteryLevel()
        val isCharging = isCharging()
        val isBatterySaverOn = isBatterySaverMode()
        val isDeviceIdleMode = isDeviceIdleMode()
        val isLowBattery = batteryLevel < LOW_BATTERY_LEVEL

        val optimizationLevel = determineOptimizationLevel(
            batteryLevel, isCharging, isBatterySaverOn, isDeviceIdleMode
        )

        val processingMultiplier = getProcessingMultiplier(optimizationLevel)
        val batchSize = getRecommendedBatchSize(optimizationLevel)

        return BatteryState(
            level = batteryLevel,
            isCharging = isCharging,
            isLowBattery = isLowBattery,
            isBatterySaverOn = isBatterySaverOn,
            isDeviceIdleMode = isDeviceIdleMode,
            optimizationLevel = optimizationLevel,
            processingMultiplier = processingMultiplier,
            recommendedBatchSize = batchSize
        )
    }

    /**
     * Get current battery level percentage
     */
    private fun getBatteryLevel(): Int {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            if (level == -1 || scale == -1) {
                Timber.w("⚠️ Could not determine battery level, assuming 50%")
                50
            } else {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting battery level")
            50 // Default to 50% if we can't determine
        }
    }

    /**
     * Check if device is currently charging
     */
    private fun isCharging(): Boolean {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking charging status")
            false
        }
    }

    /**
     * Check if battery saver mode is enabled
     */
    private fun isBatterySaverMode(): Boolean {
        return try {
            powerManager?.isPowerSaveMode == true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking battery saver mode")
            false
        }
    }

    /**
     * Check if device is in doze mode (idle mode)
     */
    private fun isDeviceIdleMode(): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                powerManager?.isDeviceIdleMode == true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking device idle mode")
            false
        }
    }

    /**
     * Determine optimization level based on battery state
     */
    private fun determineOptimizationLevel(
        batteryLevel: Int,
        isCharging: Boolean,
        isBatterySaverOn: Boolean,
        isDeviceIdleMode: Boolean
    ): OptimizationLevel {
        return when {
            // If charging, use normal processing (unless battery saver is explicitly on)
            isCharging && !isBatterySaverOn -> OptimizationLevel.NORMAL

            // If battery saver is on or device is in doze mode, use aggressive optimization
            isBatterySaverOn || isDeviceIdleMode -> OptimizationLevel.AGGRESSIVE

            // Battery level based optimization
            batteryLevel < CRITICAL_BATTERY_LEVEL -> OptimizationLevel.AGGRESSIVE
            batteryLevel < LOW_BATTERY_LEVEL -> OptimizationLevel.MODERATE
            batteryLevel < MEDIUM_BATTERY_LEVEL -> OptimizationLevel.LIGHT
            else -> OptimizationLevel.NORMAL
        }
    }

    /**
     * Get processing interval multiplier based on optimization level
     */
    private fun getProcessingMultiplier(level: OptimizationLevel): Float {
        return when (level) {
            OptimizationLevel.NORMAL -> NORMAL_PROCESSING_MULTIPLIER
            OptimizationLevel.LIGHT -> LIGHT_OPTIMIZATION_MULTIPLIER
            OptimizationLevel.MODERATE -> MODERATE_OPTIMIZATION_MULTIPLIER
            OptimizationLevel.AGGRESSIVE -> AGGRESSIVE_OPTIMIZATION_MULTIPLIER
        }
    }

    /**
     * Get recommended batch size based on optimization level
     */
    private fun getRecommendedBatchSize(level: OptimizationLevel): Int {
        return when (level) {
            OptimizationLevel.NORMAL -> NORMAL_BATCH_SIZE
            OptimizationLevel.LIGHT -> NORMAL_BATCH_SIZE
            OptimizationLevel.MODERATE -> OPTIMIZED_BATCH_SIZE
            OptimizationLevel.AGGRESSIVE -> AGGRESSIVE_BATCH_SIZE
        }
    }

    /**
     * Check if we should process notifications now based on battery state
     */
    fun shouldProcessNotifications(): Boolean {
        val batteryState = getCurrentBatteryState()

        return when (batteryState.optimizationLevel) {
            OptimizationLevel.NORMAL, OptimizationLevel.LIGHT, OptimizationLevel.MODERATE -> true
            OptimizationLevel.AGGRESSIVE -> {
                // Only skip if battery is critically low AND not charging
                batteryState.level > CRITICAL_BATTERY_LEVEL || batteryState.isCharging
            }
        }
    }

    /**
     * Get optimal processing interval in seconds based on current battery state
     */
    fun getOptimalProcessingInterval(): Long {
        val batteryState = getCurrentBatteryState()
        val baseInterval = 15L // Base 15 seconds
        val calculatedInterval = (baseInterval * batteryState.processingMultiplier).toLong()
        
        // Ensure interval is always between 2-5 seconds for near real-time processing
        return when {
            calculatedInterval < 2L -> 2L
            calculatedInterval > 5L -> 5L
            else -> calculatedInterval
        }
    }

    /**
     * Log current battery optimization status
     */
    fun logBatteryOptimizationStatus() {
        val batteryState = getCurrentBatteryState()

        Timber.i("🔋 Battery Optimization Status:")
        Timber.i("  - Battery Level: ${batteryState.level}%")
        Timber.i("  - Charging: ${batteryState.isCharging}")
        Timber.i("  - Battery Saver: ${batteryState.isBatterySaverOn}")
        Timber.i("  - Device Idle: ${batteryState.isDeviceIdleMode}")
        Timber.i("  - Optimization Level: ${batteryState.optimizationLevel}")
        Timber.i("  - Processing Multiplier: ${batteryState.processingMultiplier}x")
        Timber.i("  - Recommended Batch Size: ${batteryState.recommendedBatchSize}")
        Timber.i("  - Optimal Interval: ${getOptimalProcessingInterval()}s")
        Timber.i("  - Should Process: ${shouldProcessNotifications()}")
    }

    /**
     * Check if device is in a battery-critical state where processing should be paused
     */
    fun isInCriticalBatteryState(): Boolean {
        val batteryState = getCurrentBatteryState()
        return batteryState.level < CRITICAL_BATTERY_LEVEL &&
               !batteryState.isCharging &&
               batteryState.isBatterySaverOn
    }
}