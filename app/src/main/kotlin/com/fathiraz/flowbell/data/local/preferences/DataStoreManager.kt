package com.fathiraz.flowbell.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import com.fathiraz.flowbell.core.utils.LoggerUtils
import com.fathiraz.flowbell.data.preferences.PreferenceKeys
import com.fathiraz.flowbell.data.preferences.toPreferenceValue
import com.fathiraz.flowbell.data.preferences.toThemeMode
import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.entities.ThemeMode
import java.io.IOException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

/**
 * DataStore extension for creating preferences datastore with corruption handler
 * Specifically designed to handle Samsung eMMC corruption and storage issues
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flowbell_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { exception ->
            LoggerUtils.DataStore.e("DataStore corruption detected, creating new preferences", exception)
            emptyPreferences()
        }
    )
)

/**
 * Manager class for DataStore operations with error handling and logging
 * Enhanced for Samsung device compatibility with fallback mechanisms
 */
class DataStoreManager(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    // Fallback SharedPreferences for critical failures (Samsung-specific)
    private val fallbackPrefs by lazy {
        context.getSharedPreferences("flowbell_fallback_prefs", Context.MODE_PRIVATE)
    }

    // Samsung device detection for enhanced logging
    private val isSamsungDevice: Boolean by lazy {
        android.os.Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    init {
        if (isSamsungDevice) {
            LoggerUtils.DataStore.i("Samsung device detected: ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})")
            LoggerUtils.DataStore.i("Enhanced persistence mechanisms activated for Samsung compatibility")
        }
    }

    /**
     * Get user preferences as a Flow
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }

    /**
     * Update theme mode preference
     */
    suspend fun updateThemeMode(themeMode: ThemeMode): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.THEME_MODE] = themeMode.toPreferenceValue()
            }
            Timber.d("Theme mode updated to: $themeMode")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to update theme mode")
            Result.failure(exception)
        }
    }

    /**
     * Update first launch preference
     */
    suspend fun updateFirstLaunch(isFirstLaunch: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.IS_FIRST_LAUNCH] = isFirstLaunch
            }
            Timber.d("First launch updated to: $isFirstLaunch")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to update first launch preference")
            Result.failure(exception)
        }
    }

    /**
     * Update notifications enabled preference
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
            }
            Timber.d("Notifications enabled updated to: $enabled")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to update notifications preference")
            Result.failure(exception)
        }
    }

    /**
     * Update webhook URL preference with Samsung-specific fallback
     */
    suspend fun updateWebhookUrl(url: String): Result<Unit> {
        return try {
            // Primary: Try DataStore
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.WEBHOOK_URL] = url
            }

            // Secondary: Also save to fallback SharedPreferences for Samsung reliability
            try {
                fallbackPrefs.edit()
                    .putString("webhook_url_fallback", url)
                    .apply()
            } catch (e: Exception) {
                Timber.w(e, "Fallback storage failed, but primary succeeded")
            }

            val logMsg = if (isSamsungDevice) {
                "Webhook URL updated successfully (DataStore + Samsung fallback) - Device: ${android.os.Build.MODEL}"
            } else {
                "Webhook URL updated successfully (DataStore + fallback)"
            }
            LoggerUtils.DataStore.d(logMsg)
            Result.success(Unit)
        } catch (exception: IOException) {
            val warningMsg = if (isSamsungDevice) {
                "DataStore failed on Samsung device ${android.os.Build.MODEL}, trying fallback storage"
            } else {
                "DataStore failed, trying fallback storage"
            }
            Timber.w(exception, warningMsg)

            // Fallback: Use SharedPreferences if DataStore fails
            return try {
                fallbackPrefs.edit()
                    .putString("webhook_url_fallback", url)
                    .commit() // Use commit() for immediate write

                val successMsg = if (isSamsungDevice) {
                    "Webhook URL saved using fallback storage (Samsung ${android.os.Build.MODEL} compatibility)"
                } else {
                    "Webhook URL saved using fallback storage"
                }
                Timber.i(successMsg)
                Result.success(Unit)
            } catch (fallbackException: Exception) {
                val errorMsg = if (isSamsungDevice) {
                    "Both DataStore and fallback failed on Samsung ${android.os.Build.MODEL}"
                } else {
                    "Both DataStore and fallback failed"
                }
                Timber.e(fallbackException, errorMsg)
                Result.failure(fallbackException)
            }
        }
    }

    /**
     * Update auto start service preference
     */
    suspend fun updateAutoStartService(autoStart: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.AUTO_START_SERVICE] = autoStart
            }
            Timber.d("Auto start service updated to: $autoStart")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to update auto start service preference")
            Result.failure(exception)
        }
    }

    /**
     * Update onboarding completion preference
     */
    suspend fun updateOnboardingCompleted(completed: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.IS_ONBOARDING_COMPLETED] = completed
            }
            Timber.d("Onboarding completion status updated to: $completed")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to update onboarding completion preference")
            Result.failure(exception)
        }
    }

    /**
     * Clear webhook URL from both DataStore and fallback
     */
    suspend fun clearWebhookUrl(): Result<Unit> {
        return try {
            // Clear from DataStore
            dataStore.edit { preferences ->
                preferences.remove(PreferenceKeys.WEBHOOK_URL)
            }

            // Also clear from fallback
            try {
                fallbackPrefs.edit()
                    .remove("webhook_url_fallback")
                    .apply()
            } catch (e: Exception) {
                Timber.w(e, "Failed to clear from fallback storage")
            }

            Timber.d("Webhook URL cleared from all storage")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to clear webhook URL")
            Result.failure(exception)
        }
    }

    /**
     * Clear all preferences - enhanced for fresh install scenarios
     */
    suspend fun clearAllPreferences(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.clear()
            }

            // Also clear fallback SharedPreferences
            try {
                fallbackPrefs.edit().clear().apply()
            } catch (e: Exception) {
                Timber.w(e, "Failed to clear fallback storage")
            }

            // Clear any other app-specific shared preferences that might persist
            try {
                context.getSharedPreferences("flowbell_preferences", Context.MODE_PRIVATE)
                    .edit().clear().apply()
            } catch (e: Exception) {
                Timber.w(e, "Failed to clear main shared preferences")
            }

            Timber.d("All preferences cleared (DataStore + fallback + legacy)")
            Result.success(Unit)
        } catch (exception: IOException) {
            Timber.e(exception, "Failed to clear preferences")
            Result.failure(exception)
        }
    }

    /**
     * Detect and handle fresh install by checking if database exists but DataStore is empty
     */
    suspend fun handleFreshInstallIfNeeded(): Result<Unit> {
        return try {
            // Check if this is a fresh install by looking for app version SharedPreference
            val appVersionPrefs = context.getSharedPreferences("app_version", Context.MODE_PRIVATE)
            val lastKnownVersion = appVersionPrefs.getString("version_code", null)
            val currentVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
            } catch (e: Exception) {
                "unknown"
            }

            val isFreshInstall = lastKnownVersion == null

            if (isFreshInstall) {
                Timber.i("Fresh install detected - clearing all persistent storage")
                
                // Clear all preferences to ensure clean state
                clearAllPreferences().getOrThrow()
                
                // Mark this version as installed
                appVersionPrefs.edit()
                    .putString("version_code", currentVersion)
                    .putLong("install_timestamp", System.currentTimeMillis())
                    .apply()
                
                Timber.i("Fresh install setup completed")
            } else {
                Timber.d("Existing installation detected - version: $lastKnownVersion")
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Timber.e(exception, "Error handling fresh install detection")
            Result.failure(exception)
        }
    }

    /**
     * Map preferences to UserPreferences data class with fallback support
     */
    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val themeMode = preferences[PreferenceKeys.THEME_MODE].toThemeMode()
        val isFirstLaunch = preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: true
        val notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true

        // Enhanced webhook URL reading with fallback for Samsung devices
        val webhookUrl = preferences[PreferenceKeys.WEBHOOK_URL] ?: run {
            // If DataStore doesn't have it, check fallback SharedPreferences
            val fallbackUrl = try {
                fallbackPrefs.getString("webhook_url_fallback", "") ?: ""
            } catch (e: Exception) {
                Timber.w(e, "Failed to read from fallback storage")
                ""
            }

            if (fallbackUrl.isNotEmpty()) {
                Timber.i("Retrieved webhook URL from fallback storage (Samsung compatibility)")
            }
            fallbackUrl
        }

        val autoStartService = preferences[PreferenceKeys.AUTO_START_SERVICE] ?: false
        val isOnboardingCompleted = preferences[PreferenceKeys.IS_ONBOARDING_COMPLETED] ?: false

        return UserPreferences(
            themeMode = themeMode,
            isFirstLaunch = isFirstLaunch,
            notificationsEnabled = notificationsEnabled,
            webhookUrl = webhookUrl,
            autoStartService = autoStartService,
            isOnboardingCompleted = isOnboardingCompleted
        )
    }
}