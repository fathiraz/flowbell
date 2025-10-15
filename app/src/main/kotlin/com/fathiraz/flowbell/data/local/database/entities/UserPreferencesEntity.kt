package com.fathiraz.flowbell.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.entities.UserPreferences

/**
 * Room entity for storing user preferences in SQLite database
 * Provides reliable persistence across Android versions 10-15
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for user preferences

    @ColumnInfo(name = "theme_mode")
    val themeMode: String = ThemeMode.SYSTEM.name,

    @ColumnInfo(name = "is_first_launch")
    val isFirstLaunch: Boolean = true,

    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true,

    @ColumnInfo(name = "webhook_url")
    val webhookUrl: String = "",

    @ColumnInfo(name = "auto_start_service")
    val autoStartService: Boolean = false,

    @ColumnInfo(name = "is_onboarding_completed")
    val isOnboardingCompleted: Boolean = false,

    @ColumnInfo(name = "is_debug_mode_enabled")
    val isDebugModeEnabled: Boolean = false,

    @ColumnInfo(name = "notification_filter_enabled")
    val notificationFilterEnabled: Boolean = false,

    @ColumnInfo(name = "global_filter_words")
    val globalFilterWords: String = "", // Stored as comma-separated

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert entity to domain model
 */
fun UserPreferencesEntity.toDomainModel(): UserPreferences {
    return UserPreferences(
        themeMode = try {
            ThemeMode.valueOf(themeMode)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        },
        isFirstLaunch = isFirstLaunch,
        notificationsEnabled = notificationsEnabled,
        webhookUrl = webhookUrl,
        autoStartService = autoStartService,
        isOnboardingCompleted = isOnboardingCompleted,
        isDebugModeEnabled = isDebugModeEnabled,
        notificationFilterEnabled = notificationFilterEnabled,
        keywordFilters = globalFilterWords.split(",").filter { it.isNotBlank() }
    )
}

/**
 * Extension function to convert domain model to entity
 */
fun UserPreferences.toEntity(): UserPreferencesEntity {
    return UserPreferencesEntity(
        themeMode = themeMode.name,
        isFirstLaunch = isFirstLaunch,
        notificationsEnabled = notificationsEnabled,
        webhookUrl = webhookUrl,
        autoStartService = autoStartService,
        isOnboardingCompleted = isOnboardingCompleted,
        isDebugModeEnabled = isDebugModeEnabled,
        notificationFilterEnabled = notificationFilterEnabled,
        globalFilterWords = keywordFilters.joinToString(","),
        updatedAt = System.currentTimeMillis()
    )
}