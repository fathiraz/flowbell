package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification-related operations.
 * This interface defines the contract for notification data operations.
 */
interface NotificationRepository {
    
    /**
     * Save a new notification.
     */
    suspend fun saveNotification(notification: Notification): Result<Unit>
    
    /**
     * Get notifications by package name.
     */
    suspend fun getNotificationsByPackage(packageName: String): Flow<List<Notification>>
    
    /**
     * Get all notifications.
     */
    suspend fun getAllNotifications(): Flow<List<Notification>>
    
    /**
     * Get notifications within date range.
     */
    suspend fun getNotificationsInRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<Notification>>
    
    /**
     * Delete old notifications (cleanup).
     */
    suspend fun deleteOldNotifications(olderThanDays: Int): Result<Int>
    
    /**
     * Get notification by ID.
     */
    suspend fun getNotificationById(id: String): Result<Notification>
}
