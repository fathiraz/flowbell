package com.fathiraz.flowbell.data.local.database.dao

import androidx.room.*
import com.fathiraz.flowbell.data.local.database.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for user preferences operations
 * Provides reliable database operations for Android 10-15 compatibility
 */
@Dao
interface UserPreferencesDao {

    /**
     * Get user preferences as a Flow for reactive updates
     */
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>

    /**
     * Get user preferences synchronously (for initial setup)
     */
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferences(): UserPreferencesEntity?

    /**
     * Insert or update user preferences
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: UserPreferencesEntity)

    /**
     * Update theme mode
     */
    @Query("UPDATE user_preferences SET theme_mode = :themeMode, updated_at = :timestamp WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update first launch flag
     */
    @Query("UPDATE user_preferences SET is_first_launch = :isFirstLaunch, updated_at = :timestamp WHERE id = 1")
    suspend fun updateFirstLaunch(isFirstLaunch: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update notifications enabled
     */
    @Query("UPDATE user_preferences SET notifications_enabled = :enabled, updated_at = :timestamp WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update webhook URL - critical for Samsung device compatibility
     */
    @Query("UPDATE user_preferences SET webhook_url = :url, updated_at = :timestamp WHERE id = 1")
    suspend fun updateWebhookUrl(url: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update auto start service
     */
    @Query("UPDATE user_preferences SET auto_start_service = :autoStart, updated_at = :timestamp WHERE id = 1")
    suspend fun updateAutoStartService(autoStart: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update onboarding completion status
     */
    @Query("UPDATE user_preferences SET is_onboarding_completed = :completed, updated_at = :timestamp WHERE id = 1")
    suspend fun updateOnboardingCompleted(completed: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Clear webhook URL
     */
    @Query("UPDATE user_preferences SET webhook_url = '', updated_at = :timestamp WHERE id = 1")
    suspend fun clearWebhookUrl(timestamp: Long = System.currentTimeMillis())

    /**
     * Delete all preferences (for testing or reset)
     */
    @Query("DELETE FROM user_preferences")
    suspend fun clearAllPreferences()

    /**
     * Check if preferences exist
     */
    @Query("SELECT COUNT(*) FROM user_preferences WHERE id = 1")
    suspend fun preferencesExist(): Int

    /**
     * Get webhook URL specifically (for debugging)
     */
    @Query("SELECT webhook_url FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getWebhookUrl(): String?

    /**
     * Transaction: Ensure preferences exist, create default if not
     */
    @Transaction
    suspend fun ensurePreferencesExist() {
        if (preferencesExist() == 0) {
            insertOrUpdatePreferences(UserPreferencesEntity())
        }
    }
}