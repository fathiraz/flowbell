package com.fathiraz.flowbell.domain.usecases

import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import timber.log.Timber

/**
 * Use case for managing user settings.
 * This use case encapsulates the business logic for user preferences management.
 */
class ManageUserSettingsUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Get user preferences.
     */
    suspend fun getUserPreferences(): kotlinx.coroutines.flow.Flow<UserPreferences> {
        Timber.d("ManageUserSettingsUseCase: Getting user preferences")
        return userPreferencesRepository.getUserPreferences()
    }
    
    /**
     * Update theme mode.
     */
    suspend fun updateThemeMode(themeMode: ThemeMode): Result<Unit> {
        Timber.d("ManageUserSettingsUseCase: Updating theme mode to $themeMode")
        
        return try {
            val result = userPreferencesRepository.updateThemeMode(themeMode)
            if (result.isSuccess) {
                Timber.d("ManageUserSettingsUseCase: Successfully updated theme mode")
            } else {
                Timber.e("ManageUserSettingsUseCase: Failed to update theme mode")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageUserSettingsUseCase: Exception updating theme mode")
            Result.failure(e)
        }
    }
    
    /**
     * Update notification filters.
     */
    suspend fun updateNotificationFilters(
        keywordFilters: List<String>,
        categoryFilters: List<String>
    ): Result<Unit> {
        Timber.d("ManageUserSettingsUseCase: Updating notification filters")
        
        return try {
            val result = userPreferencesRepository.updateNotificationFilters(keywordFilters, categoryFilters)
            if (result.isSuccess) {
                Timber.d("ManageUserSettingsUseCase: Successfully updated notification filters")
            } else {
                Timber.e("ManageUserSettingsUseCase: Failed to update notification filters")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageUserSettingsUseCase: Exception updating notification filters")
            Result.failure(e)
        }
    }
    
    /**
     * Mark first launch as completed.
     */
    suspend fun markFirstLaunchCompleted(): Result<Unit> {
        Timber.d("ManageUserSettingsUseCase: Marking first launch as completed")
        
        return try {
            val result = userPreferencesRepository.markFirstLaunchCompleted()
            if (result.isSuccess) {
                Timber.d("ManageUserSettingsUseCase: Successfully marked first launch as completed")
            } else {
                Timber.e("ManageUserSettingsUseCase: Failed to mark first launch as completed")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageUserSettingsUseCase: Exception marking first launch completed")
            Result.failure(e)
        }
    }
    
    /**
     * Update history settings.
     */
    suspend fun updateHistorySettings(
        maxHistoryDays: Int,
        autoDeleteEnabled: Boolean
    ): Result<Unit> {
        Timber.d("ManageUserSettingsUseCase: Updating history settings")
        
        return try {
            val result = userPreferencesRepository.updateHistorySettings(maxHistoryDays, autoDeleteEnabled)
            if (result.isSuccess) {
                Timber.d("ManageUserSettingsUseCase: Successfully updated history settings")
            } else {
                Timber.e("ManageUserSettingsUseCase: Failed to update history settings")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageUserSettingsUseCase: Exception updating history settings")
            Result.failure(e)
        }
    }
}