package com.fathiraz.flowbell.data.mappers

import com.fathiraz.flowbell.data.local.database.entities.UserPreferencesEntity
import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.entities.ThemeMode

/**
 * Mapper for converting between data and domain UserPreferences entities.
 */
object UserPreferencesMapper {

    /**
     * Convert data UserPreferencesEntity to domain UserPreferences.
     */
    fun toDomain(entity: UserPreferencesEntity): UserPreferences {
        return UserPreferences(
            themeMode = try {
                ThemeMode.valueOf(entity.themeMode)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            },
            isFirstLaunch = entity.isFirstLaunch,
            notificationFilterEnabled = false, // Default for missing field
            keywordFilters = emptyList(), // Default for missing field
            categoryFilters = emptyList(), // Default for missing field
            maxHistoryDays = 30, // Default for missing field
            autoDeleteEnabled = true, // Default for missing field
            webhookUrl = entity.webhookUrl,
            notificationsEnabled = entity.notificationsEnabled,
            autoStartService = entity.autoStartService,
            isOnboardingCompleted = entity.isOnboardingCompleted
        )
    }

    /**
     * Convert domain UserPreferences to data UserPreferencesEntity.
     */
    fun toEntity(preferences: UserPreferences): UserPreferencesEntity {
        return UserPreferencesEntity(
            themeMode = preferences.themeMode.name,
            isFirstLaunch = preferences.isFirstLaunch,
            notificationsEnabled = preferences.notificationsEnabled,
            webhookUrl = preferences.webhookUrl,
            autoStartService = preferences.autoStartService,
            isOnboardingCompleted = preferences.isOnboardingCompleted,
            updatedAt = System.currentTimeMillis()
        )
    }
}