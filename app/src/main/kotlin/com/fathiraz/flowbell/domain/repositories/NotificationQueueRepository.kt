package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification queue operations
 */
interface NotificationQueueRepository {
    
    /**
     * Get recent notifications
     */
    suspend fun getRecentNotifications(
        limit: Int = 50,
        offset: Int = 0,
        status: NotificationQueueStatus? = null
    ): List<NotificationLog>

    
    /**
     * Get recent notifications with real-time updates
     */
    fun getRecentNotificationsFlow(
        limit: Int = 50,
        offset: Int = 0,
        status: NotificationQueueStatus? = null
    ): Flow<List<NotificationLog>>
    
    /**
     * Observe queue size
     */
    fun observeQueueSize(): Flow<Int>
    
    /**
     * Add notification to queue
     */
    suspend fun addToQueue(notification: NotificationLog): Result<Unit>
    
    /**
     * Update notification status
     */
    suspend fun updateStatus(id: String, status: NotificationQueueStatus): Result<Unit>
    
    /**
     * Retry failed notification
     */
    suspend fun retryNotification(id: String): Result<Unit>
    
    /**
     * Clear old notifications
     */
    suspend fun clearOldNotifications(daysOld: Int): Result<Unit>
}
