package com.fathiraz.flowbell.domain.entities

/**
 * Domain entity representing user preferences.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isFirstLaunch: Boolean = true,
    val notificationFilterEnabled: Boolean = false,
    val keywordFilters: List<String> = emptyList(),
    val categoryFilters: List<String> = emptyList(),
    val maxHistoryDays: Int = 30,
    val autoDeleteEnabled: Boolean = true,
    val webhookUrl: String = "",
    val notificationsEnabled: Boolean = true,
    val autoStartService: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

