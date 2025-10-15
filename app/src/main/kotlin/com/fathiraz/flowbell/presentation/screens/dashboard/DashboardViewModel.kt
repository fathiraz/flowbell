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
import kotlinx.coroutines.delay
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
        loadUserPreferences()
        loadStatistics()
        loadRecentActivity()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DashboardViewModel", "🔄 Loading statistics for period: ${_uiState.value.selectedTimePeriod}")
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get real statistics from repository with real-time updates
                statisticsRepository.getNotificationStatistics().collect { statistics ->
                    android.util.Log.d("DashboardViewModel", "📊 Statistics: ${statistics.totalNotifications} total, dailyStats=${statistics.dailyStats.size}, topApps=${statistics.topApps.size}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statistics = statistics,
                        errorMessage = null
                    )
                    
                    // Calculate trend data and top applications
                    calculateTrendData()
                    calculateTopApplications()
                    
                    Timber.d("Dashboard statistics updated with ${statistics.dailyStats.size} daily stats and ${statistics.topApps.size} top apps")
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "❌ Error loading statistics", e)
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

    fun selectTimePeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedTimePeriod = period)
        loadStatistics() // Reload with new period
    }

    private fun calculateTrendData() {
        val period = _uiState.value.selectedTimePeriod
        val days = when (period) {
            "24 Hours" -> 1
            "7 Days" -> 7
            "30 Days" -> 30
            else -> 7 // Default to 7 days
        }
        
        val dailyStats = _uiState.value.statistics.dailyStats
        val trendData = if (dailyStats.isNotEmpty()) {
            // Get data for the selected period, pad with zeros if needed
            val dataPoints = dailyStats.takeLast(days).map { it.totalNotifications }
            // Pad with zeros if we don't have enough data
            if (dataPoints.size < days) {
                List(days - dataPoints.size) { 0 } + dataPoints
            } else {
                dataPoints
            }
        } else {
            // Return zeros for empty data
            List(days) { 0 }
        }
        _uiState.value = _uiState.value.copy(trendData = trendData)
    }

    private fun calculateTopApplications() {
        val topApps = _uiState.value.statistics.topApps
        val totalCount = topApps.sumOf { it.count }
        
        val appStatistics = if (topApps.isNotEmpty()) {
            topApps.take(5).map { app ->
                AppStatistic(
                    appName = app.appName,
                    packageName = app.packageName,
                    count = app.count,
                    percentage = if (totalCount > 0) (app.count.toFloat() / totalCount) * 100 else 0f
                )
            }
        } else {
            // Return empty list if no real data available
            emptyList()
        }
        _uiState.value = _uiState.value.copy(topApplications = appStatistics)
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, isLoading = true)
            try {
                // Show skeleton loading during refresh
                delay(1000) // Show skeleton for 1 second

                // Reload statistics and recent activity
                loadStatistics()
                loadRecentActivity()
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun triggerImmediateProcessing() {
        // Implementation coming soon
    }

    fun forceRescheduleWorkManager() {
        // Implementation coming soon
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.getUserPreferences().collect { preferences ->
                    _uiState.value = _uiState.value.copy(
                        isDebugModeEnabled = preferences.isDebugModeEnabled
                    )
                    Timber.d("Debug mode loaded: ${preferences.isDebugModeEnabled}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user preferences")
            }
        }
    }
}

@androidx.compose.runtime.Immutable
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val statistics: NotificationStatistics = NotificationStatistics(),
    val recentActivity: List<NotificationLog> = emptyList(),
    val errorMessage: String? = null,
    val isDebugModeEnabled: Boolean = false,
    val selectedTimePeriod: String = "7 Days",
    val trendData: List<Int> = emptyList(),
    val topApplications: List<AppStatistic> = emptyList()
)

@androidx.compose.runtime.Immutable
data class AppStatistic(
    val appName: String,
    val packageName: String,
    val count: Int,
    val percentage: Float
)