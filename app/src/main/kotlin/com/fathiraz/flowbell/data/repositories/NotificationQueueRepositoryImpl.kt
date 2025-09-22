package com.fathiraz.flowbell.data.repositories

import com.fathiraz.flowbell.data.local.database.dao.NotificationQueueDao
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus as DataNotificationQueueStatus
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import com.fathiraz.flowbell.domain.repositories.NotificationQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
/**
 * Repository implementation for notification queue operations
 */
class NotificationQueueRepositoryImpl(
    private val notificationQueueDao: NotificationQueueDao
) : NotificationQueueRepository {

    override suspend fun getRecentNotifications(
        limit: Int,
        offset: Int,
        status: com.fathiraz.flowbell.domain.entities.NotificationQueueStatus?
    ): List<NotificationLog> {
        return try {
            android.util.Log.d("NotificationQueueRepository", "🔍 Getting recent notifications - limit: $limit, offset: $offset")
            val notifications = notificationQueueDao.getRecentNotifications(limit, offset)
            android.util.Log.d("NotificationQueueRepository", "📊 DAO returned ${notifications.size} notifications")

            val result = notifications.map { entity ->
                android.util.Log.d("NotificationQueueRepository", "📱 Mapping notification: ${entity.appName} - ${entity.title}")
                NotificationLog(
                    id = entity.id.toString(),
                    appName = entity.appName,
                    packageName = entity.packageName,
                    title = entity.title,
                    text = entity.text,
                    notificationTitle = entity.title,
                    notificationText = entity.text,
                    timestamp = entity.timestamp,
                    priority = entity.priority,
                    isOngoing = entity.isOngoing,
                    isClearable = entity.isClearable,
                    status = mapToDomainStatus(entity.status),
                    retryCount = entity.retryCount,
                    lastAttemptAt = entity.lastAttemptAt,
                    errorMessage = entity.errorMessage,
                    httpDetails = if (entity.httpUrl != null) {
                        com.fathiraz.flowbell.domain.entities.HttpRequestResponseDetails(
                            requestMethod = entity.httpMethod ?: "POST",
                            requestUrl = entity.httpUrl,
                            requestHeaders = emptyMap(),
                            requestBody = "",
                            responseCode = entity.httpResponseCode ?: -1,
                            responseHeaders = emptyMap(),
                            responseBody = entity.httpResponseBody,
                            timestamp = entity.timestamp,
                            duration = entity.httpDuration ?: 0L
                        )
                    } else null
                )
            }
            
            android.util.Log.d("NotificationQueueRepository", "✅ Returning ${result.size} mapped notifications")
            result
        } catch (e: Exception) {
            android.util.Log.e("NotificationQueueRepository", "❌ Error getting recent notifications", e)
            Timber.e(e, "Error getting recent notifications")
            emptyList()
        }
    }

    override fun getRecentNotificationsFlow(
        limit: Int,
        offset: Int,
        status: com.fathiraz.flowbell.domain.entities.NotificationQueueStatus?
    ): Flow<List<NotificationLog>> {
        return notificationQueueDao.getRecentNotificationsFlow(limit, offset).map { entities ->
            try {
                android.util.Log.d("NotificationQueueRepository", "📊 Real-time notifications update: ${entities.size} items")
                entities.map { entity ->
                    NotificationLog(
                        id = entity.id.toString(),
                        appName = entity.appName,
                        packageName = entity.packageName,
                        title = entity.title,
                        text = entity.text,
                        notificationTitle = entity.title,
                        notificationText = entity.text,
                        timestamp = entity.timestamp,
                        priority = entity.priority,
                        isOngoing = entity.isOngoing,
                        isClearable = entity.isClearable,
                        status = mapToDomainStatus(entity.status),
                        retryCount = entity.retryCount,
                        lastAttemptAt = entity.lastAttemptAt,
                        errorMessage = entity.errorMessage,
                        httpDetails = if (entity.httpUrl != null) {
                            com.fathiraz.flowbell.domain.entities.HttpRequestResponseDetails(
                                requestMethod = entity.httpMethod ?: "POST",
                                requestUrl = entity.httpUrl,
                                requestHeaders = emptyMap(),
                                requestBody = "",
                                responseCode = entity.httpResponseCode ?: -1,
                                responseHeaders = emptyMap(),
                                responseBody = entity.httpResponseBody,
                                timestamp = entity.timestamp,
                                duration = entity.httpDuration ?: 0L
                            )
                        } else null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationQueueRepository", "❌ Error mapping real-time notifications", e)
                Timber.e(e, "Error getting real-time recent notifications")
                emptyList()
            }
        }
    }

    override fun observeQueueSize(): Flow<Int> {
        return notificationQueueDao.observeQueueSize()
    }

    override suspend fun addToQueue(notification: NotificationLog): Result<Unit> {
        return try {
            // Convert domain entity to data entity
            val queueEntity = com.fathiraz.flowbell.data.local.database.entities.NotificationQueue(
                packageName = notification.packageName,
                appName = notification.appName,
                title = notification.notificationTitle,
                text = notification.notificationText,
                timestamp = notification.timestamp,
                priority = notification.priority,
                isOngoing = notification.isOngoing,
                isClearable = notification.isClearable,
                status = mapFromDomainStatus(notification.status),
                retryCount = notification.retryCount,
                lastAttemptAt = notification.lastAttemptAt,
                errorMessage = notification.errorMessage,
                // Map HTTP details from domain to database entity
                httpUrl = notification.httpDetails?.requestUrl,
                httpMethod = notification.httpDetails?.requestMethod,
                httpResponseCode = notification.httpDetails?.responseCode,
                httpResponseBody = notification.httpDetails?.responseBody,
                httpDuration = notification.httpDetails?.duration
            )

            notificationQueueDao.insertNotification(queueEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding notification to queue")
            Result.failure(e)
        }
    }

    override suspend fun updateStatus(
        id: String,
        status: com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
    ): Result<Unit> {
        return try {
            val entityId = id.toLongOrNull() ?: return Result.failure(
                IllegalArgumentException("Invalid notification ID: $id")
            )

            val dataStatus = mapFromDomainStatus(status)

            when (status) {
                com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.SENT -> {
                    notificationQueueDao.markNotificationAsSent(entityId)
                }
                com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.FAILED -> {
                    notificationQueueDao.markNotificationAsFailed(entityId, "Manual status update")
                }
                else -> {
                    // For PENDING and PROCESSING, we need to update manually
                    val notification = notificationQueueDao.getRecentNotifications(1).firstOrNull { it.id == entityId }
                    notification?.let {
                        val updatedNotification = it.copy(status = dataStatus)
                        notificationQueueDao.updateNotification(updatedNotification)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating notification status")
            Result.failure(e)
        }
    }

    override suspend fun retryNotification(id: String): Result<Unit> {
        return try {
            val entityId = id.toLongOrNull() ?: return Result.failure(
                IllegalArgumentException("Invalid notification ID: $id")
            )

            val notification = notificationQueueDao.getRecentNotifications(1).firstOrNull { it.id == entityId }
            notification?.let {
                val updatedNotification = it.copy(
                    status = DataNotificationQueueStatus.PENDING,
                    retryCount = it.retryCount + 1
                )
                notificationQueueDao.updateNotification(updatedNotification)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error retrying notification")
            Result.failure(e)
        }
    }

    override suspend fun clearOldNotifications(daysOld: Int): Result<Unit> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            notificationQueueDao.batchDeleteOldNotifications(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing old notifications")
            Result.failure(e)
        }
    }

    private fun mapToDomainStatus(status: DataNotificationQueueStatus): com.fathiraz.flowbell.domain.entities.NotificationQueueStatus {
        return when (status) {
            DataNotificationQueueStatus.PENDING -> com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.PENDING
            DataNotificationQueueStatus.PROCESSING -> com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.PROCESSING
            DataNotificationQueueStatus.SENT -> com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.SENT
            DataNotificationQueueStatus.FAILED -> com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.FAILED
        }
    }

    private fun mapFromDomainStatus(status: com.fathiraz.flowbell.domain.entities.NotificationQueueStatus): DataNotificationQueueStatus {
        return when (status) {
            com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.PENDING -> DataNotificationQueueStatus.PENDING
            com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.PROCESSING -> DataNotificationQueueStatus.PROCESSING
            com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.SENT -> DataNotificationQueueStatus.SENT
            com.fathiraz.flowbell.domain.entities.NotificationQueueStatus.FAILED -> DataNotificationQueueStatus.FAILED
        }
    }
}