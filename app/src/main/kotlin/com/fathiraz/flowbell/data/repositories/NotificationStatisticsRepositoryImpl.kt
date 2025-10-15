package com.fathiraz.flowbell.data.repositories

import com.fathiraz.flowbell.data.local.database.dao.NotificationQueueDao
import com.fathiraz.flowbell.domain.entities.NotificationStatistics
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import com.fathiraz.flowbell.presentation.screens.analytics.AppStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.DailyStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.HourlyStatistic
import com.fathiraz.flowbell.presentation.screens.analytics.StatusStatistic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import timber.log.Timber
import com.fathiraz.flowbell.domain.entities.DailyNotificationStats
import com.fathiraz.flowbell.domain.entities.AppNotificationCount

/**
 * Repository implementation for notification statistics operations
 */
class NotificationStatisticsRepositoryImpl(
    private val notificationQueueDao: NotificationQueueDao
) : NotificationStatisticsRepository {

    override suspend fun getNotificationStatistics(): Flow<NotificationStatistics> {
        return notificationQueueDao.getQueueStatsFlow().map { queueStats ->
            try {
                val total = queueStats.sumOf { it.count }
                val successful = queueStats.find { it.status == com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus.SENT }?.count ?: 0
                val failed = queueStats.find { it.status == com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus.FAILED }?.count ?: 0
                val pending = queueStats.find { it.status == com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus.PENDING }?.count ?: 0
                
                // Get last 7 days for default statistics
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (7 * 24 * 60 * 60 * 1000L)
                
                // Fetch daily stats and top apps
                val dailyStatsFlow = notificationQueueDao.getDailyStatisticsFlow(startTime, endTime)
                val topAppsFlow = notificationQueueDao.getTopApplicationsFlow(startTime, endTime, 5)
                
                val dailyStats = dailyStatsFlow.first().map { result ->
                    DailyNotificationStats(
                        date = java.time.LocalDate.parse(result.date),
                        totalNotifications = result.totalNotifications,
                        successfulDeliveries = result.successfulDeliveries,
                        failedDeliveries = result.failedDeliveries
                    )
                }
                
                val topApps = topAppsFlow.first().map { result ->
                    AppNotificationCount(
                        packageName = result.packageName,
                        appName = result.appName,
                        count = result.count
                    )
                }
                
                val statistics = NotificationStatistics(
                    totalNotifications = total,
                    successfulDeliveries = successful,
                    failedDeliveries = failed,
                    pending = pending,
                    successRate = if (total > 0) (successful.toDouble() / total.toDouble()) * 100.0 else 0.0,
                    dailyStats = dailyStats,
                    topApps = topApps
                )
                
                android.util.Log.d("NotificationStatisticsRepositoryImpl", "📊 Statistics with data: total=$total, dailyStats=${dailyStats.size}, topApps=${topApps.size}")
                statistics
            } catch (e: Exception) {
                Timber.e(e, "Error calculating statistics")
                android.util.Log.e("NotificationStatisticsRepositoryImpl", "❌ Error calculating statistics", e)
                NotificationStatistics()
            }
        }
    }

    override suspend fun getStatisticsInRange(
        startDate: Long,
        endDate: Long
    ): Flow<NotificationStatistics> {
        return flowOf(
            NotificationStatistics(
                totalNotifications = 0,
                successful = 0,
                failed = 0,
                pending = 0
            )
        )
    }

    override suspend fun getTopAppsByNotificationCount(limit: Int): Flow<List<com.fathiraz.flowbell.domain.entities.AppNotificationCount>> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L) // Last 7 days
        
        return notificationQueueDao.getTopApplicationsFlow(startTime, endTime, limit).map { results ->
            results.map { result ->
                AppNotificationCount(
                    packageName = result.packageName,
                    appName = result.appName,
                    count = result.count
                )
            }
        }
    }

    override suspend fun getDailyStatistics(days: Int): Flow<List<com.fathiraz.flowbell.domain.entities.DailyNotificationStats>> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        return notificationQueueDao.getDailyStatisticsFlow(startTime, endTime).map { results ->
            results.map { result ->
                DailyNotificationStats(
                    date = java.time.LocalDate.parse(result.date),
                    totalNotifications = result.totalNotifications,
                    successfulDeliveries = result.successfulDeliveries,
                    failedDeliveries = result.failedDeliveries
                )
            }
        }
    }

    override suspend fun refreshStatistics(): Result<Unit> {
        return try {
            Timber.i("Statistics refreshed")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh statistics")
            Result.failure(e)
        }
    }

    override fun getTotalNotificationsCount(): Flow<Long> {
        return notificationQueueDao.getQueueStatsFlow().map { queueStats ->
            try {
                val total = queueStats.sumOf { it.count }.toLong()
                android.util.Log.d("NotificationStatisticsRepositoryImpl", "📊 Real-time total count: $total")
                total
            } catch (e: Exception) {
                Timber.e(e, "Error getting total notifications count")
                android.util.Log.e("NotificationStatisticsRepositoryImpl", "❌ Error getting total count", e)
                0L
            }
        }
    }

    override fun getSuccessfulWebhooksCount(): Flow<Long> {
        return notificationQueueDao.getCountByStatusFlow("SENT").map { count ->
            try {
                android.util.Log.d("NotificationStatisticsRepositoryImpl", "📊 Real-time successful count: $count")
                count.toLong()
            } catch (e: Exception) {
                Timber.e(e, "Error getting successful webhooks count")
                android.util.Log.e("NotificationStatisticsRepositoryImpl", "❌ Error getting successful count", e)
                0L
            }
        }
    }

    override fun getFailedWebhooksCount(): Flow<Long> {
        return notificationQueueDao.getCountByStatusFlow("FAILED").map { count ->
            try {
                android.util.Log.d("NotificationStatisticsRepositoryImpl", "📊 Real-time failed count: $count")
                count.toLong()
            } catch (e: Exception) {
                Timber.e(e, "Error getting failed webhooks count")
                android.util.Log.e("NotificationStatisticsRepositoryImpl", "❌ Error getting failed count", e)
                0L
            }
        }
    }

    override fun getTopAppsStats(limit: Int): Flow<List<AppStatistic>> {
        return flowOf(emptyList())
    }

    override fun getDailyStats(days: Int): Flow<List<DailyStatistic>> {
        return flowOf(emptyList())
    }

    override fun getHourlyDistribution(): Flow<List<HourlyStatistic>> {
        return flowOf(emptyList())
    }

    override fun getNotificationsByStatus(): Flow<List<StatusStatistic>> {
        return flowOf(emptyList())
    }
}