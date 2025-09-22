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
import timber.log.Timber

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
                
                val statistics = NotificationStatistics(
                    totalNotifications = total,
                    successfulDeliveries = successful,
                    failedDeliveries = failed,
                    successRate = if (total > 0) (successful.toDouble() / total.toDouble()) * 100.0 else 0.0
                )
                
                android.util.Log.d("NotificationStatisticsRepositoryImpl", "📊 Real-time statistics: total=$total, successful=$successful, failed=$failed")
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
        return flowOf(emptyList())
    }

    override suspend fun getDailyStatistics(days: Int): Flow<List<com.fathiraz.flowbell.domain.entities.DailyNotificationStats>> {
        return flowOf(emptyList())
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