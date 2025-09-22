package com.fathiraz.flowbell.presentation.state

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalNotifications: Int = 0,
    val successfulWebhooks: Int = 0,
    val failedWebhooks: Int = 0,
    val successRate: Float = 0f,
    val selectedDays: Int = 7,
    val topApps: List<AppStatistic> = emptyList(),
    val dailyStats: List<DailyStatistic> = emptyList(),
    val hourlyDistribution: List<HourlyStatistic> = emptyList(),
    val notificationsByStatus: List<StatusStatistic> = emptyList(),
    val lastUpdated: String = ""
)