package com.fathiraz.flowbell.presentation.screens.settings

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.ThemeMode
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.data.preferences.ThemePreferences
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
    val isNotificationFiltersEnabled: Boolean = false,
    val isDebugModeEnabled: Boolean = false,
    val globalFilterWords: List<String> = emptyList(),
    val filterWordsInput: String = ""
)

sealed interface SettingsEvent {
    object ToggleTheme : SettingsEvent
    object ToggleNotificationFilters : SettingsEvent
    object ToggleDebugMode : SettingsEvent
    object ClearError : SettingsEvent
    data class UpdateFilterWordsInput(val input: String) : SettingsEvent
    object SaveFilterWords : SettingsEvent
    object ClearFilterWords : SettingsEvent
}

class SettingsViewModel(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Use ThemePreferences for dark mode
    private val themePreferences = ThemePreferences(context)

    init {
        // Observe theme preferences changes
        viewModelScope.launch {
            themePreferences.isDarkMode
                .onEach { isDarkMode ->
                    _uiState.value = _uiState.value.copy(isDarkMode = isDarkMode)
                    Timber.d("Dark mode updated: $isDarkMode")
                }
                .launchIn(viewModelScope)
        }

        // Observe user preferences changes from DataStore
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences()
                .onEach { preferences ->
                    _uiState.value = _uiState.value.copy(
                        isNotificationFiltersEnabled = preferences.notificationFilterEnabled,
                        isDebugModeEnabled = preferences.isDebugModeEnabled,
                        globalFilterWords = preferences.keywordFilters,
                        filterWordsInput = preferences.keywordFilters.joinToString(", ")
                    )

                    // Initialize debug tools state based on user preferences
                    DebugToolsManager.setDebugModeEnabled(context, preferences.isDebugModeEnabled)

                    Timber.d("User preferences loaded: notification filters = %s, debug mode = %s, filter words = %s", 
                        preferences.notificationFilterEnabled, preferences.isDebugModeEnabled, preferences.keywordFilters.size)
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ToggleTheme -> {
                viewModelScope.launch {
                    try {
                        val newDarkMode = !_uiState.value.isDarkMode
                        themePreferences.setDarkMode(newDarkMode)
                        Timber.d("Theme toggled to ${if (newDarkMode) "dark" else "light"} mode")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle theme")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to change theme")
                    }
                }
            }
            SettingsEvent.ToggleNotificationFilters -> {
                viewModelScope.launch {
                    try {
                        val newState = !_uiState.value.isNotificationFiltersEnabled
                        val result = userPreferencesRepository.updateNotificationFilterEnabled(newState)
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        Timber.d("Notification filters toggled to $newState")
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
            is SettingsEvent.UpdateFilterWordsInput -> {
                _uiState.value = _uiState.value.copy(filterWordsInput = event.input)
            }
            SettingsEvent.SaveFilterWords -> {
                viewModelScope.launch {
                    try {
                        val filterWords = _uiState.value.filterWordsInput
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        
                        val result = userPreferencesRepository.updateNotificationFilters(filterWords, emptyList())
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            globalFilterWords = filterWords,
                            errorMessage = null
                        )
                        
                        Timber.d("Filter words saved: ${filterWords.joinToString(",")}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to save filter words")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to save filter words")
                    }
                }
            }
            SettingsEvent.ClearFilterWords -> {
                viewModelScope.launch {
                    try {
                        val result = userPreferencesRepository.updateNotificationFilters(emptyList(), emptyList())
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            globalFilterWords = emptyList(),
                            filterWordsInput = "",
                            errorMessage = null
                        )
                        
                        Timber.d("Filter words cleared")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to clear filter words")
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to clear filter words")
                    }
                }
            }
        }
    }
}