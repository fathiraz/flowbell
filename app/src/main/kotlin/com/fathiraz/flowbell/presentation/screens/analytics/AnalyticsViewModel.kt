package com.fathiraz.flowbell.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import com.fathiraz.flowbell.core.utils.LoggerUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
/**
 * ViewModel for Analytics Dashboard
 * Provides comprehensive notification statistics and insights
 */
class AnalyticsViewModel(
    private val statisticsRepository: NotificationStatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun onEvent(event: AnalyticsEvent) {
        when (event) {
            AnalyticsEvent.Refresh -> loadAnalytics()
            is AnalyticsEvent.UpdateDateRange -> updateDateRange(event.days)
            AnalyticsEvent.ClearData -> clearAnalyticsData()
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Collect data from multiple sources
                val totalFlow = statisticsRepository.getTotalNotificationsCount()
                val successfulFlow = statisticsRepository.getSuccessfulWebhooksCount()
                val failedFlow = statisticsRepository.getFailedWebhooksCount()
                val topAppsFlow = statisticsRepository.getTopAppsStats(limit = 10)
                val dailyFlow = statisticsRepository.getDailyStats(days = _uiState.value.selectedDays)
                val hourlyFlow = statisticsRepository.getHourlyDistribution()
                val statusFlow = statisticsRepository.getNotificationsByStatus()

                combine(
                    totalFlow,
                    successfulFlow,
                    failedFlow,
                    topAppsFlow,
                    dailyFlow,
                    hourlyFlow,
                    statusFlow
                ) { flows ->
                    val total = flows[0] as Long
                    val successful = flows[1] as Long
                    val failed = flows[2] as Long
                    val topApps = flows[3] as List<AppStatistic>
                    val daily = flows[4] as List<DailyStatistic>
                    val hourly = flows[5] as List<HourlyStatistic>
                    val byStatus = flows[6] as List<StatusStatistic>

                    LoggerUtils.Database.d("Analytics loaded - Total: $total, Success: $successful, Failed: $failed")

                    AnalyticsUiState(
                        isLoading = false,
                        totalNotifications = total,
                        successfulWebhooks = successful,
                        failedWebhooks = failed,
                        successRate = if (total > 0) ((successful.toDouble() / total) * 100).toInt() else 0,
                        topApps = topApps,
                        dailyStats = daily,
                        hourlyDistribution = hourly,
                        notificationsByStatus = byStatus,
                        selectedDays = _uiState.value.selectedDays,
                        lastUpdated = java.time.LocalDateTime.now().toString()
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                LoggerUtils.Database.e("Failed to load analytics", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }

    private fun updateDateRange(days: Int) {
        _uiState.value = _uiState.value.copy(selectedDays = days)
        loadAnalytics()
    }

    private fun clearAnalyticsData() {
        viewModelScope.launch {
            try {
                // Note: Implement clear functionality in repository if needed
                LoggerUtils.Database.i("Analytics data clear requested")
                loadAnalytics() // Refresh after clear
            } catch (e: Exception) {
                LoggerUtils.Database.e("Failed to clear analytics data", e)
            }
        }
    }
}

/**
 * UI State for Analytics Dashboard
 */
data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val totalNotifications: Long = 0,
    val successfulWebhooks: Long = 0,
    val failedWebhooks: Long = 0,
    val successRate: Int = 0,
    val topApps: List<AppStatistic> = emptyList(),
    val dailyStats: List<DailyStatistic> = emptyList(),
    val hourlyDistribution: List<HourlyStatistic> = emptyList(),
    val notificationsByStatus: List<StatusStatistic> = emptyList(),
    val selectedDays: Int = 7,
    val lastUpdated: String = "",
    val errorMessage: String? = null
)

/**
 * Analytics Events
 */
sealed interface AnalyticsEvent {
    object Refresh : AnalyticsEvent
    object ClearData : AnalyticsEvent
    data class UpdateDateRange(val days: Int) : AnalyticsEvent
}

/**
 * Data classes for statistics
 */
data class AppStatistic(
    val packageName: String,
    val appName: String,
    val notificationCount: Long,
    val successfulWebhooks: Long,
    val failedWebhooks: Long,
    val successRate: Int
)

data class DailyStatistic(
    val date: String,
    val notificationCount: Long,
    val successfulWebhooks: Long,
    val failedWebhooks: Long
)

data class HourlyStatistic(
    val hour: Int,
    val notificationCount: Long
)

data class StatusStatistic(
    val status: String,
    val count: Long,
    val percentage: Int
)