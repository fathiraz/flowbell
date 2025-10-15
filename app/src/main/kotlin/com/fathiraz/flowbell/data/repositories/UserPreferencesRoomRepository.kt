package com.fathiraz.flowbell.data.repositories

import com.fathiraz.flowbell.data.local.database.dao.UserPreferencesDao
import com.fathiraz.flowbell.data.local.database.entities.UserPreferencesEntity
import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-based implementation of UserPreferencesRepository
 */
class UserPreferencesRoomRepository(
    private val userPreferencesDao: UserPreferencesDao
) : UserPreferencesRepository {

    override suspend fun getUserPreferences(): Flow<UserPreferences> =
        userPreferencesDao.getUserPreferencesFlow().map { entity ->
            entity?.let {
                UserPreferences(
                    themeMode = try { ThemeMode.valueOf(it.themeMode) } catch (e: Exception) { ThemeMode.SYSTEM },
                    isFirstLaunch = it.isFirstLaunch,
                    notificationsEnabled = it.notificationsEnabled,
                    webhookUrl = it.webhookUrl,
                    autoStartService = it.autoStartService,
                    isOnboardingCompleted = it.isOnboardingCompleted,
                    isDebugModeEnabled = it.isDebugModeEnabled,
                    notificationFilterEnabled = it.notificationFilterEnabled,
                    keywordFilters = it.globalFilterWords.split(",").filter { word -> word.isNotBlank() }
                )
            } ?: UserPreferences()
        }

    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.insertOrUpdatePreferences(
                UserPreferencesEntity(
                    themeMode = preferences.themeMode.name,
                    isFirstLaunch = preferences.isFirstLaunch,
                    notificationsEnabled = preferences.notificationsEnabled,
                    webhookUrl = preferences.webhookUrl,
                    autoStartService = preferences.autoStartService,
                    isOnboardingCompleted = preferences.isOnboardingCompleted,
                    isDebugModeEnabled = preferences.isDebugModeEnabled,
                    globalFilterWords = preferences.keywordFilters.joinToString(","),
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateThemeMode(themeMode.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationFilters(keywordFilters: List<String>, categoryFilters: List<String>): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateGlobalFilterWords(keywordFilters.joinToString(","))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markFirstLaunchCompleted(): Result<Unit> {
        return updateFirstLaunch(false)
    }

    override suspend fun updateHistorySettings(maxHistoryDays: Int, autoDeleteEnabled: Boolean): Result<Unit> {
        // TODO: Implement history settings in Room schema
        return Result.success(Unit)
    }

    override suspend fun updateWebhookUrl(webhookUrl: String): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateWebhookUrl(webhookUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFirstLaunch(isFirstLaunch: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateFirstLaunch(isFirstLaunch)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateNotificationsEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAutoStartService(autoStart: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateAutoStartService(autoStart)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateOnboardingCompleted(completed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetToDefaults(): Result<Unit> {
        return try {
            userPreferencesDao.clearAllPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDebugMode(isEnabled: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateDebugMode(isEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationFilterEnabled(isEnabled: Boolean): Result<Unit> {
        return try {
            userPreferencesDao.ensurePreferencesExist()
            userPreferencesDao.updateNotificationFilterEnabled(isEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get global filter words for notification filtering
     */
    suspend fun getGlobalFilterWords(): List<String> {
        return try {
            val entity = userPreferencesDao.getUserPreferences()
            entity?.globalFilterWords?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}