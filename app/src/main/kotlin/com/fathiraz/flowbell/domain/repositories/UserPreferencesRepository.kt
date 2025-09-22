package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences operations.
 * This interface defines the contract for user preferences data operations.
 */
interface UserPreferencesRepository {

    /**
     * Get user preferences as a Flow.
     */
    suspend fun getUserPreferences(): Flow<UserPreferences>

    /**
     * Update user preferences.
     */
    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit>

    /**
     * Update theme mode.
     */
    suspend fun updateThemeMode(themeMode: com.fathiraz.flowbell.domain.entities.ThemeMode): Result<Unit>

    /**
     * Update notification filters.
     */
    suspend fun updateNotificationFilters(
        keywordFilters: List<String>,
        categoryFilters: List<String>
    ): Result<Unit>

    /**
     * Mark first launch as completed.
     */
    suspend fun markFirstLaunchCompleted(): Result<Unit>

    /**
     * Update history settings.
     */
    suspend fun updateHistorySettings(
        maxHistoryDays: Int,
        autoDeleteEnabled: Boolean
    ): Result<Unit>

    /**
     * Update webhook URL.
     */
    suspend fun updateWebhookUrl(webhookUrl: String): Result<Unit>

    /**
     * Update first launch status.
     */
    suspend fun updateFirstLaunch(isFirstLaunch: Boolean): Result<Unit>

    /**
     * Update notifications enabled.
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit>

    /**
     * Update auto start service.
     */
    suspend fun updateAutoStartService(autoStart: Boolean): Result<Unit>

    /**
     * Set onboarding completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>

    /**
     * Reset to defaults.
     */
    suspend fun resetToDefaults(): Result<Unit>
}
