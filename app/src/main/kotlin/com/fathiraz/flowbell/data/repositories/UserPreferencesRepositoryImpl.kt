package com.fathiraz.flowbell.data.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.fathiraz.flowbell.data.preferences.DataStoreManager
import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository

/**
 * Implementation of UserPreferencesRepository using DataStore
 * Handles all user preference operations with proper error handling and logging
 */
class UserPreferencesRepositoryImpl(
    private val dataStoreManager: DataStoreManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    override suspend fun getUserPreferences(): Flow<UserPreferences> = dataStoreManager.userPreferencesFlow

    override suspend fun updateThemeMode(themeMode: ThemeMode): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.updateThemeMode(themeMode)
                if (result.isSuccess) {
                    Timber.i("Theme mode successfully updated to: $themeMode")
                } else {
                    Timber.w("Failed to update theme mode: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating theme mode")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateFirstLaunch(isFirstLaunch: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.updateFirstLaunch(isFirstLaunch)
                if (result.isSuccess) {
                    Timber.i("First launch status successfully updated to: $isFirstLaunch")
                } else {
                    Timber.w("Failed to update first launch status: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating first launch status")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.updateNotificationsEnabled(enabled)
                if (result.isSuccess) {
                    Timber.i("Notifications enabled successfully updated to: $enabled")
                } else {
                    Timber.w("Failed to update notifications setting: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating notifications setting")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateWebhookUrl(url: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                // Basic URL validation
                val trimmedUrl = url.trim()
                if (trimmedUrl.isNotEmpty() && !isValidUrl(trimmedUrl)) {
                    Timber.w("Invalid webhook URL format: $trimmedUrl")
                    return@withContext Result.failure(IllegalArgumentException("Invalid URL format"))
                }

                val result = dataStoreManager.updateWebhookUrl(trimmedUrl)
                if (result.isSuccess) {
                    Timber.i("Webhook URL successfully updated")
                } else {
                    Timber.w("Failed to update webhook URL: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating webhook URL")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateAutoStartService(autoStart: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.updateAutoStartService(autoStart)
                if (result.isSuccess) {
                    Timber.i("Auto start service successfully updated to: $autoStart")
                } else {
                    Timber.w("Failed to update auto start service: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating auto start service")
                Result.failure(exception)
            }
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.updateOnboardingCompleted(completed)
                if (result.isSuccess) {
                    Timber.i("Onboarding completion status successfully updated to: $completed")
                } else {
                    Timber.w("Failed to update onboarding completion status: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating onboarding completion status")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                // Update all preferences individually
                updateThemeMode(preferences.themeMode).getOrThrow()
                updateNotificationsEnabled(preferences.notificationsEnabled).getOrThrow()
                updateWebhookUrl(preferences.webhookUrl).getOrThrow()
                updateAutoStartService(preferences.autoStartService).getOrThrow()

                Result.success(Unit)
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error updating user preferences")
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateNotificationFilters(
        keywordFilters: List<String>,
        categoryFilters: List<String>
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                // TODO: Implement keyword and category filters in DataStore
                Timber.i("Notification filters updated: keywords=${keywordFilters.size}, categories=${categoryFilters.size}")
                Result.success(Unit)
            } catch (exception: Exception) {
                Timber.e(exception, "Error updating notification filters")
                Result.failure(exception)
            }
        }
    }

    override suspend fun markFirstLaunchCompleted(): Result<Unit> {
        return updateFirstLaunch(false)
    }

    override suspend fun updateHistorySettings(
        maxHistoryDays: Int,
        autoDeleteEnabled: Boolean
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                // TODO: Implement history settings in DataStore
                Timber.i("History settings updated: maxDays=$maxHistoryDays, autoDelete=$autoDeleteEnabled")
                Result.success(Unit)
            } catch (exception: Exception) {
                Timber.e(exception, "Error updating history settings")
                Result.failure(exception)
            }
        }
    }

    override suspend fun resetToDefaults(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = dataStoreManager.clearAllPreferences()
                if (result.isSuccess) {
                    Timber.i("All preferences successfully reset to defaults")
                } else {
                    Timber.w("Failed to reset preferences: ${result.exceptionOrNull()}")
                }
                result
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error resetting preferences")
                Result.failure(exception)
            }
        }
    }

    /**
     * Basic URL validation helper
     */
    private fun isValidUrl(url: String): Boolean {
        return try {
            android.util.Patterns.WEB_URL.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }
}