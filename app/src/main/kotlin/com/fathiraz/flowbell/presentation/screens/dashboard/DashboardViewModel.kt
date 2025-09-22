package com.fathiraz.flowbell.presentation.screens.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.NotificationStatistics
import com.fathiraz.flowbell.domain.entities.StatisticsPeriod
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import com.fathiraz.flowbell.domain.repositories.NotificationQueueRepository
// import com.fathiraz.flowbell.core.utils.NotificationWorkManagerUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class DashboardViewModel(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val statisticsRepository: NotificationStatisticsRepository,
    private val notificationQueueRepository: NotificationQueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // private val _selectedPeriod = MutableStateFlow(StatisticsPeriod.LAST_24_HOURS)
    // val selectedPeriod: StateFlow<StatisticsPeriod> = _selectedPeriod.asStateFlow()

    // private val notificationWorkManager = NotificationWorkManagerUtils.NotificationWorkManager(context)

    init {
        android.util.Log.d("DashboardViewModel", "🚀 DashboardViewModel initialized with real-time statistics and recent activity")
        loadStatistics()
        loadRecentActivity()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DashboardViewModel", "🔄 Setting up real-time statistics monitoring")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Get real statistics from repository with real-time updates
                statisticsRepository.getNotificationStatistics().collect { statistics ->
                    android.util.Log.d("DashboardViewModel", "📊 Real-time statistics update: ${statistics.totalNotifications} total, ${statistics.successfulDeliveries} successful")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statistics = statistics,
                        errorMessage = null
                    )
                    
                    Timber.d("Dashboard statistics updated: ${statistics.totalNotifications} total notifications")
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "❌ Error in real-time statistics", e)
                Timber.e(e, "Failed to load dashboard statistics")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load statistics: ${e.message}"
                )
            }
        }
    }

    fun updatePeriod(period: String) {
        // _selectedPeriod.value = period
        loadStatistics()
    }

    private fun loadRecentActivity() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DashboardViewModel", "🔄 Setting up real-time recent activity monitoring")

                // Get recent notifications with real-time updates (limit to 5 for dashboard)
                notificationQueueRepository.getRecentNotificationsFlow(
                    limit = 5,
                    offset = 0
                ).collect { recentNotifications ->
                    android.util.Log.d("DashboardViewModel", "📋 Real-time recent activity update: ${recentNotifications.size} items")
                    _uiState.value = _uiState.value.copy(
                        recentActivity = recentNotifications
                    )

                    Timber.d("Dashboard recent activity updated: ${recentNotifications.size} notifications")
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "❌ Error in real-time recent activity", e)
                Timber.e(e, "Failed to load dashboard recent activity")
                // Don't update error state for recent activity failures
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
        loadRecentActivity()
    }

    fun triggerImmediateProcessing() {
        // Implementation coming soon
    }

    fun forceRescheduleWorkManager() {
        // Implementation coming soon
    }
}

@androidx.compose.runtime.Immutable
data class DashboardUiState(
    val isLoading: Boolean = false,
    val statistics: NotificationStatistics = NotificationStatistics(),
    val recentActivity: List<NotificationLog> = emptyList(),
    val errorMessage: String? = null
)