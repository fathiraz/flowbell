package com.fathiraz.flowbell.domain.entities

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain entity representing notification statistics.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class NotificationStatistics(
    val totalNotifications: Int = 0,
    val successful: Int = 0,
    val failed: Int = 0,
    val pending: Int = 0,
    val processing: Int = 0,
    val successfulDeliveries: Int = 0,
    val failedDeliveries: Int = 0,
    val successRate: Double = 0.0,
    val failureRate: Double = 0.0,
    val queueHealth: QueueHealth = QueueHealth.HEALTHY,
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val topFailingApps: List<AppFailureStatistic> = emptyList(),
    val recentActivity: List<RecentActivity> = emptyList(),
    val topApps: List<AppNotificationCount> = emptyList(),
    val dailyStats: List<DailyNotificationStats> = emptyList()
)

/**
 * Represents notification count for a specific app.
 */
data class AppNotificationCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

/**
 * Represents daily notification statistics.
 */
data class DailyNotificationStats(
    val date: LocalDate,
    val totalNotifications: Int,
    val successfulDeliveries: Int,
    val failedDeliveries: Int
)

/**
 * Represents weekly notification statistics.
 */
data class WeeklyStats(
    val currentWeekTotal: Int = 0,
    val previousWeekTotal: Int = 0,
    val weeklyTrend: Double = 0.0,
    val dailyTotals: List<Int> = emptyList(),
    val dailySuccess: List<Int> = emptyList(),
    val trend: StatisticsTrend = StatisticsTrend.STABLE,
    val warnings: List<String> = emptyList(),
    val queueSize: Int = 0
)

/**
 * Enum representing statistics trend.
 */
enum class StatisticsTrend {
    UP,
    DOWN,
    STABLE,
    IMPROVING,
    DECLINING
}

/**
 * Represents app failure information.
 */
data class AppFailureInfo(
    val packageName: String,
    val appName: String,
    val failureCount: Int,
    val lastFailure: LocalDateTime? = null,
    val errorType: String? = null
)

/**
 * Represents recent activity information.
 */
data class ActivityInfo(
    val timestamp: LocalDateTime,
    val activityType: String,
    val description: String,
    val packageName: String? = null,
    val success: Boolean = true
)

/**
 * Represents app failure statistics for the dashboard.
 */
data class AppFailureStatistic(
    val packageName: String,
    val appName: String,
    val failureCount: Int,
    val totalCount: Int,
    val failureRate: Double
)

/**
 * Represents recent activity for the dashboard.
 */
data class RecentActivity(
    val timestamp: Long,
    val appName: String,
    val title: String,
    val status: NotificationQueueStatus
)

// Type alias for backward compatibility
typealias WeeklyStatistics = WeeklyStats
