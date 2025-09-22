package com.fathiraz.flowbell.domain.usecases

import com.fathiraz.flowbell.domain.entities.Notification
import com.fathiraz.flowbell.domain.repositories.NotificationRepository
import timber.log.Timber

/**
 * Use case for handling notification queue operations.
 * This use case encapsulates the business logic for notification queuing and processing.
 */
class HandleNotificationQueueUseCase(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Save notification to queue.
     */
    suspend fun saveNotification(notification: Notification): Result<Unit> {
        Timber.d("HandleNotificationQueueUseCase: Saving notification ${notification.id}")
        
        return try {
            val result = notificationRepository.saveNotification(notification)
            if (result.isSuccess) {
                Timber.d("HandleNotificationQueueUseCase: Successfully saved notification ${notification.id}")
            } else {
                Timber.e("HandleNotificationQueueUseCase: Failed to save notification ${notification.id}")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "HandleNotificationQueueUseCase: Exception saving notification")
            Result.failure(e)
        }
    }
    
    /**
     * Get notifications by package name.
     */
    suspend fun getNotificationsByPackage(packageName: String): kotlinx.coroutines.flow.Flow<List<Notification>> {
        Timber.d("HandleNotificationQueueUseCase: Getting notifications for package $packageName")
        return notificationRepository.getNotificationsByPackage(packageName)
    }
    
    /**
     * Get all notifications.
     */
    suspend fun getAllNotifications(): kotlinx.coroutines.flow.Flow<List<Notification>> {
        Timber.d("HandleNotificationQueueUseCase: Getting all notifications")
        return notificationRepository.getAllNotifications()
    }
    
    /**
     * Clean up old notifications.
     */
    suspend fun cleanupOldNotifications(olderThanDays: Int): Result<Int> {
        Timber.d("HandleNotificationQueueUseCase: Cleaning up notifications older than $olderThanDays days")
        
        return try {
            val result = notificationRepository.deleteOldNotifications(olderThanDays)
            if (result.isSuccess) {
                val deletedCount = result.getOrNull() ?: 0
                Timber.d("HandleNotificationQueueUseCase: Cleaned up $deletedCount old notifications")
            } else {
                Timber.e("HandleNotificationQueueUseCase: Failed to cleanup old notifications")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "HandleNotificationQueueUseCase: Exception cleaning up notifications")
            Result.failure(e)
        }
    }
}