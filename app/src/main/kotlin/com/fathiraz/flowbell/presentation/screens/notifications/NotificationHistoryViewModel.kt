package com.fathiraz.flowbell.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.domain.repositories.NotificationQueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * ViewModel for Notification History with real-time updates
 */
class NotificationHistoryViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationQueueRepository: NotificationQueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("NotificationHistoryViewModel", "🚀 NotificationHistoryViewModel initialized with real-time updates")
        setupRealtimeUpdates()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.FilterByStatus -> filterByStatus(event.status)
            is HistoryEvent.SearchByApp -> searchByApp(event.query)
            is HistoryEvent.ShowNotificationDetails -> showNotificationDetails(event.notificationLog)
            HistoryEvent.Refresh -> refreshNotifications()
            HistoryEvent.ClearFilters -> clearFilters()
            HistoryEvent.ToggleFilters -> toggleFilters()
            HistoryEvent.LoadMore -> loadMoreNotifications()
        }
    }

    private fun setupRealtimeUpdates() {
        android.util.Log.d("NotificationHistoryViewModel", "🔄 Setting up real-time updates")
        viewModelScope.launch {
            try {
                // Set loading state
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Add timeout to prevent infinite loading
                launch {
                    delay(5000) // 5 second timeout
                    if (_uiState.value.isLoading) {
                        android.util.Log.w("NotificationHistoryViewModel", "⚠️ Loading timeout reached, clearing loading state")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }

                notificationQueueRepository.getRecentNotificationsFlow(
                    limit = _uiState.value.pageSize,
                    offset = 0
                ).collect { notifications ->
                    android.util.Log.d("NotificationHistoryViewModel", "📊 Real-time update received: ${notifications.size} notifications")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        logs = notifications,
                        filteredLogs = notifications,
                        hasMoreItems = notifications.size >= _uiState.value.pageSize
                    )

                    // Apply any existing filters
                    applyFilters()
                    android.util.Log.d("NotificationHistoryViewModel", "✅ Real-time UI state updated - logs size: ${notifications.size}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationHistoryViewModel", "❌ Error in real-time updates", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to setup real-time updates: ${e.message}"
                )
            }
        }
    }

    private fun loadNotifications() {
        android.util.Log.d("NotificationHistoryViewModel", "🔄 Manual loadNotifications() called - real-time updates should handle this automatically")
        // Real-time updates handle this automatically, but we can trigger a refresh
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // The real-time flow will automatically update the UI
        }
    }

    private fun refreshNotifications() {
        android.util.Log.d("NotificationHistoryViewModel", "🔄 Pull-to-refresh triggered")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, isLoading = true, error = null)
            try {
                // Show skeleton loading during refresh
                delay(1000) // Show skeleton for 1 second

                // Force reload by restarting the flow
                setupRealtimeUpdates()
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    private fun loadMoreNotifications() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMoreItems) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingMore = true)

            try {
                // Load more notifications using offset
                val moreNotifications = notificationQueueRepository.getRecentNotifications(
                    limit = currentState.pageSize,
                    offset = currentState.currentPage * currentState.pageSize
                )

                val allNotifications = currentState.logs + moreNotifications
                val hasMore = moreNotifications.size >= currentState.pageSize

                _uiState.value = currentState.copy(
                    isLoadingMore = false,
                    logs = allNotifications,
                    filteredLogs = allNotifications,
                    currentPage = currentState.currentPage + 1,
                    hasMoreItems = hasMore
                )

                // Apply any existing filters
                applyFilters()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoadingMore = false,
                    error = "Failed to load more notifications: ${e.message}"
                )
            }
        }
    }

    private fun filterByStatus(status: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
        applyFilters()
    }

    private fun searchByApp(query: String) {
        _uiState.value = _uiState.value.copy(searchApp = query)
        applyFilters()
    }

    private fun showNotificationDetails(notificationLog: NotificationLog) {
        // Implementation for showing notification details
    }

    private fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filterStatus = null,
            searchApp = "",
            searchQuery = ""
        )
        applyFilters()
    }

    private fun toggleFilters() {
        _uiState.value = _uiState.value.copy(
            showFilters = !_uiState.value.showFilters
        )
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.logs

        // Apply status filter
        currentState.filterStatus?.let { status ->
            filtered = filtered.filter { it.status.name.equals(status, ignoreCase = true) }
        }

        // Apply app search
        if (currentState.searchApp.isNotBlank()) {
            filtered = filtered.filter {
                it.appName.contains(currentState.searchApp, ignoreCase = true) ||
                it.packageName.contains(currentState.searchApp, ignoreCase = true)
            }
        }

        _uiState.value = currentState.copy(filteredLogs = filtered)
        android.util.Log.d("NotificationHistoryViewModel", "🔧 Filters applied - final filtered logs size: ${filtered.size}")
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val logs: List<NotificationLog> = emptyList(),
    val filterStatus: String? = null,
    val searchApp: String = "",
    val searchQuery: String = "",
    val filteredLogs: List<NotificationLog> = emptyList(),
    val error: String? = null,
    val showFilters: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = true,
    val currentPage: Int = 0,
    val pageSize: Int = 20  // Reduced from 50 to 20 for faster initial load
)

sealed interface HistoryEvent {
    data class FilterByStatus(val status: String?) : HistoryEvent
    data class SearchByApp(val query: String) : HistoryEvent
    data class ShowNotificationDetails(val notificationLog: NotificationLog) : HistoryEvent
    object Refresh : HistoryEvent
    object ClearFilters : HistoryEvent
    object ToggleFilters : HistoryEvent
    object LoadMore : HistoryEvent
}