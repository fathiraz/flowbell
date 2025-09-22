package com.fathiraz.flowbell.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.App
import com.fathiraz.flowbell.domain.repositories.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

data class AppListUiState(
    val isLoading: Boolean = false,
    val apps: List<App> = emptyList(),
    val searchQuery: String = "",
    val filteredApps: List<App> = emptyList(),
    val error: String? = null,
    val showSystemApps: Boolean = false
)

sealed interface AppListEvent {
    data class ToggleApp(val packageName: String, val enabled: Boolean) : AppListEvent
    data class UpdateSearch(val query: String) : AppListEvent
    object ToggleSystemApps : AppListEvent
    object RefreshApps : AppListEvent
}

class AppListViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState(isLoading = true))
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                appRepository.getAllApps().collect { allApps ->
                    val filteredApps = if (_uiState.value.showSystemApps) {
                        allApps
                    } else {
                        allApps.filter { !it.isSystemApp }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        apps = allApps,
                        filteredApps = applySearchFilter(filteredApps, _uiState.value.searchQuery)
                    )
                    
                    Timber.d("Loaded ${allApps.size} apps (${filteredApps.size} after filtering)")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load apps"
                )
                Timber.e(e, "Failed to load apps")
            }
        }
    }
    
    private fun applySearchFilter(apps: List<App>, query: String): List<App> {
        return if (query.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    fun onEvent(event: AppListEvent) {
        when (event) {
            is AppListEvent.ToggleApp -> {
                viewModelScope.launch {
                    try {
                        Timber.d("Toggling app ${event.packageName} to ${event.enabled}")
                        
                        // Update the app forwarding status via repository
                        appRepository.updateAppForwardingStatus(event.packageName, event.enabled)
                        
                        // Reload apps to get updated state
                        loadApps()
                        
                        Timber.d("App ${event.packageName} forwarding ${if (event.enabled) "enabled" else "disabled"}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to toggle app ${event.packageName}")
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to update app preference: ${e.message}"
                        )
                    }
                }
            }
            is AppListEvent.UpdateSearch -> {
                val currentApps = if (_uiState.value.showSystemApps) {
                    _uiState.value.apps
                } else {
                    _uiState.value.apps.filter { !it.isSystemApp }
                }
                
                val filtered = applySearchFilter(currentApps, event.query)
                _uiState.value = _uiState.value.copy(
                    searchQuery = event.query,
                    filteredApps = filtered
                )
            }
            AppListEvent.ToggleSystemApps -> {
                val showSystemApps = !_uiState.value.showSystemApps
                val currentApps = if (showSystemApps) {
                    _uiState.value.apps
                } else {
                    _uiState.value.apps.filter { !it.isSystemApp }
                }
                
                val filtered = applySearchFilter(currentApps, _uiState.value.searchQuery)
                _uiState.value = _uiState.value.copy(
                    showSystemApps = showSystemApps,
                    filteredApps = filtered
                )
            }
            AppListEvent.RefreshApps -> {
                loadApps()
            }
        }
    }
}