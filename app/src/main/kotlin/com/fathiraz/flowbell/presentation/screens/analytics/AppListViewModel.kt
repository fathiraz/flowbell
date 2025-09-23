package com.fathiraz.flowbell.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.App
import com.fathiraz.flowbell.domain.repositories.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber

data class AppListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
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
                    val filteredApps = filterAndSortApps(
                        apps = allApps,
                        includeSystemApps = _uiState.value.showSystemApps,
                        query = _uiState.value.searchQuery
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        apps = allApps,
                        filteredApps = filteredApps
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

    private fun refreshApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, isLoading = true)
            try {
                // Show skeleton loading during refresh
                delay(1000) // Show skeleton for 1 second

                // Reload apps
                loadApps()
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    private fun filterAndSortApps(
        apps: List<App>,
        includeSystemApps: Boolean,
        query: String
    ): List<App> {
        val visibleApps = if (includeSystemApps) {
            apps
        } else {
            apps.filter { !it.isSystemApp }
        }

        val filteredApps = if (query.isBlank()) {
            visibleApps
        } else {
            visibleApps.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        }

        return sortApps(filteredApps)
    }

    private fun sortApps(apps: List<App>): List<App> {
        return apps.sortedWith(
            compareByDescending<App> { it.isForwardingEnabled }
                .thenBy { it.name.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
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
                val filtered = filterAndSortApps(
                    apps = _uiState.value.apps,
                    includeSystemApps = _uiState.value.showSystemApps,
                    query = event.query
                )
                _uiState.value = _uiState.value.copy(
                    searchQuery = event.query,
                    filteredApps = filtered
                )
            }
            AppListEvent.ToggleSystemApps -> {
                val showSystemApps = !_uiState.value.showSystemApps
                val filtered = filterAndSortApps(
                    apps = _uiState.value.apps,
                    includeSystemApps = showSystemApps,
                    query = _uiState.value.searchQuery
                )
                _uiState.value = _uiState.value.copy(
                    showSystemApps = showSystemApps,
                    filteredApps = filtered
                )
            }
            AppListEvent.RefreshApps -> {
                refreshApps()
            }
        }
    }
}
