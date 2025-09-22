package com.fathiraz.flowbell.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fathiraz.flowbell.domain.entities.ThemeMode

/**
 * Preference keys for DataStore
 */
object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val WEBHOOK_URL = stringPreferencesKey("webhook_url")
    val AUTO_START_SERVICE = booleanPreferencesKey("auto_start_service")
    val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
}

/**
 * Extension functions for ThemeMode conversion
 */
fun ThemeMode.toPreferenceValue(): String = this.name

fun String?.toThemeMode(): ThemeMode {
    return when (this) {
        "LIGHT" -> ThemeMode.LIGHT
        "DARK" -> ThemeMode.DARK
        "SYSTEM" -> ThemeMode.SYSTEM
        else -> ThemeMode.SYSTEM
    }
}