package com.fathiraz.flowbell.presentation.screens.settings

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.core.utils.DebugToolsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@Immutable
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPrivacyOptionsEnabled: Boolean = false,
    val isNotificationFiltersEnabled: Boolean = false,
    val isDebugModeEnabled: Boolean = false
)

sealed interface SettingsEvent {
    object ToggleTheme : SettingsEvent
    object TogglePrivacyOptions : SettingsEvent
    object ToggleNotificationFilters : SettingsEvent
    object ToggleDebugMode : SettingsEvent
    object ClearError : SettingsEvent
}

class SettingsViewModel(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe user preferences changes from DataStore
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences()
                .onEach { preferences ->
                    val isDarkMode = when (preferences.themeMode) {
                        ThemeMode.DARK -> true
                        ThemeMode.LIGHT -> false
                        ThemeMode.SYSTEM -> false // Default to light for system, or could detect system theme
                    }
                    _uiState.value = _uiState.value.copy(
                        isDarkMode = isDarkMode,
                        isDebugModeEnabled = preferences.isDebugModeEnabled
                    )

                    // Initialize debug tools state based on user preferences
                    DebugToolsManager.setDebugModeEnabled(context, preferences.isDebugModeEnabled)

                    Timber.d("Preferences loaded: theme mode = %s, dark mode = %s, debug mode = %s",
                        preferences.themeMode, isDarkMode, preferences.isDebugModeEnabled)
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ToggleTheme -> {
                viewModelScope.launch {
                    try {
                        val newThemeMode = if (_uiState.value.isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
                        val result = userPreferencesRepository.updateThemeMode(newThemeMode)
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        Timber.d("Theme toggled to %s mode", newThemeMode)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle theme")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to change theme")
                    }
                }
            }
            SettingsEvent.TogglePrivacyOptions -> {
                viewModelScope.launch {
                    try {
                        val currentState = _uiState.value
                        _uiState.value = currentState.copy(
                            isPrivacyOptionsEnabled = !currentState.isPrivacyOptionsEnabled
                        )
                        Timber.d("Privacy options toggled to ${_uiState.value.isPrivacyOptionsEnabled}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle privacy options")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to update privacy options")
                    }
                }
            }
            SettingsEvent.ToggleNotificationFilters -> {
                viewModelScope.launch {
                    try {
                        val currentState = _uiState.value
                        _uiState.value = currentState.copy(
                            isNotificationFiltersEnabled = !currentState.isNotificationFiltersEnabled
                        )
                        Timber.d("Notification filters toggled to ${_uiState.value.isNotificationFiltersEnabled}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle notification filters")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to update notification filters")
                    }
                }
            }
            SettingsEvent.ToggleDebugMode -> {
                viewModelScope.launch {
                    try {
                        val newDebugMode = !_uiState.value.isDebugModeEnabled
                        val result = userPreferencesRepository.updateDebugMode(newDebugMode)
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }

                        // Control debug tools based on the new state
                        DebugToolsManager.setDebugModeEnabled(context, newDebugMode)

                        Timber.d("Debug mode toggled to $newDebugMode")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle debug mode")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to update debug mode")
                    }
                }
            }
            SettingsEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
}