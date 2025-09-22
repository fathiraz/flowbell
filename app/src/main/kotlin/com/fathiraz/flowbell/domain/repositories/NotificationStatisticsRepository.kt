package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.NotificationStatistics
import com.fathiraz.flowbell.presentation.screens.analytics.AppStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.DailyStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.HourlyStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.StatusStatistic
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification statistics operations.
 * This interface defines the contract for statistics data operations.
 */
interface NotificationStatisticsRepository {

    /**
     * Get notification statistics.
     */
    suspend fun getNotificationStatistics(): Flow<NotificationStatistics>

    /**
     * Get statistics for a specific date range.
     */
    suspend fun getStatisticsInRange(
        startDate: Long,
        endDate: Long
    ): Flow<NotificationStatistics>

    /**
     * Get top apps by notification count.
     */
    suspend fun getTopAppsByNotificationCount(limit: Int = 10): Flow<List<com.fathiraz.flowbell.domain.entities.AppNotificationCount>>

    /**
     * Get daily statistics.
     */
    suspend fun getDailyStatistics(days: Int = 30): Flow<List<com.fathiraz.flowbell.domain.entities.DailyNotificationStats>>

    /**
     * Refresh statistics data.
     */
    suspend fun refreshStatistics(): Result<Unit>

    /**
     * Analytics methods for dashboard
     */
    fun getTotalNotificationsCount(): Flow<Long>
    fun getSuccessfulWebhooksCount(): Flow<Long>
    fun getFailedWebhooksCount(): Flow<Long>
    fun getTopAppsStats(limit: Int): Flow<List<AppStatistic>>
    fun getDailyStats(days: Int): Flow<List<DailyStatistic>>
    fun getHourlyDistribution(): Flow<List<HourlyStatistic>>
    fun getNotificationsByStatus(): Flow<List<StatusStatistic>>
}