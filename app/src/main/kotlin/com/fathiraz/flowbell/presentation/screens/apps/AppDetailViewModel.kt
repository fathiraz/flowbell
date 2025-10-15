package com.fathiraz.flowbell.presentation.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.fathiraz.flowbell.data.local.database.repository.AppPreferencesRepository

data class AppDetailUiState(
    val packageName: String = "",
    val appName: String = "",
    val isForwardingEnabled: Boolean = false,
    val filterWordsInput: String = "",
    val activeFilterWords: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface AppDetailEvent {
    data class LoadApp(val packageName: String, val appName: String) : AppDetailEvent
    data class ToggleForwarding(val enabled: Boolean) : AppDetailEvent
    data class UpdateFilterWordsInput(val input: String) : AppDetailEvent
    object SaveFilterWords : AppDetailEvent
    object ClearFilterWords : AppDetailEvent
}

class AppDetailViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: AppDetailEvent) {
        when (event) {
            is AppDetailEvent.LoadApp -> {
                loadAppPreferences(event.packageName, event.appName)
            }
            is AppDetailEvent.ToggleForwarding -> {
                toggleForwarding(event.enabled)
            }
            is AppDetailEvent.UpdateFilterWordsInput -> {
                _uiState.value = _uiState.value.copy(filterWordsInput = event.input)
            }
            AppDetailEvent.SaveFilterWords -> {
                saveFilterWords()
            }
            AppDetailEvent.ClearFilterWords -> {
                clearFilterWords()
            }
        }
    }

    private fun loadAppPreferences(packageName: String, appName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    packageName = packageName,
                    appName = appName
                )

                // Get current forwarding status
                val isForwardingEnabled = appPreferencesRepository.getForwardingStatus(packageName)
                
                // Get current filter words
                val filterWords = appPreferencesRepository.getFilterWords(packageName)
                val filterWordsInput = filterWords.joinToString(", ")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isForwardingEnabled = isForwardingEnabled,
                    activeFilterWords = filterWords,
                    filterWordsInput = filterWordsInput
                )

                Timber.d("Loaded preferences for $packageName: forwarding=$isForwardingEnabled, filters=${filterWords.size}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load app preferences"
                )
                Timber.e(e, "Failed to load preferences for $packageName")
            }
        }
    }

    private fun toggleForwarding(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val packageName = _uiState.value.packageName
                appPreferencesRepository.setForwardingEnabled(packageName, enabled)
                
                _uiState.value = _uiState.value.copy(isForwardingEnabled = enabled)
                Timber.d("Toggled forwarding for $packageName to $enabled")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update forwarding status"
                )
                Timber.e(e, "Failed to toggle forwarding for ${_uiState.value.packageName}")
            }
        }
    }

    private fun saveFilterWords() {
        viewModelScope.launch {
            try {
                val packageName = _uiState.value.packageName
                val filterWordsInput = _uiState.value.filterWordsInput
                
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                val filterWords = filterWordsInput
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                appPreferencesRepository.updateFilterWords(packageName, filterWords)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    activeFilterWords = filterWords
                )
                
                Timber.d("Saved filter words for $packageName: ${filterWords.joinToString(",")}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save filter words"
                )
                Timber.e(e, "Failed to save filter words for ${_uiState.value.packageName}")
            }
        }
    }

    private fun clearFilterWords() {
        viewModelScope.launch {
            try {
                val packageName = _uiState.value.packageName
                
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                appPreferencesRepository.updateFilterWords(packageName, emptyList())
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    filterWordsInput = "",
                    activeFilterWords = emptyList()
                )
                
                Timber.d("Cleared filter words for $packageName")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to clear filter words"
                )
                Timber.e(e, "Failed to clear filter words for ${_uiState.value.packageName}")
            }
        }
    }
}
