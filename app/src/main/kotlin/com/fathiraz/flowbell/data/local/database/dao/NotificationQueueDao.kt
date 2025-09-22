package com.fathiraz.flowbell.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.fathiraz.flowbell.data.local.database.entities.NotificationQueue

/**
 * DAO for accessing notification queue data
 */
@Dao
interface NotificationQueueDao {

    /**
     * Insert a notification into the queue
     */
    @Insert
    suspend fun insertNotification(notification: NotificationQueue): Long

    /**
     * Get pending notifications for batch processing
     * Limited to reasonable batch size to prevent memory issues
     */
    @Query("""
        SELECT * FROM notification_queue
        WHERE status = 'PENDING'
        ORDER BY createdAt ASC
        LIMIT :batchSize
    """)
    suspend fun getPendingNotifications(batchSize: Int = 50): List<NotificationQueue>

    /**
     * Get notifications that failed but can be retried
     * Only include notifications that haven't exceeded max retry attempts
     */
    @Query("""
        SELECT * FROM notification_queue
        WHERE status = 'FAILED'
        AND retryCount < :maxRetries
        AND (lastAttemptAt IS NULL OR lastAttemptAt < :retryAfterTime)
        ORDER BY createdAt ASC
        LIMIT :batchSize
    """)
    suspend fun getRetryableNotifications(
        maxRetries: Int = 3,
        retryAfterTime: Long,
        batchSize: Int = 20
    ): List<NotificationQueue>

    /**
     * Update notification status
     */
    @Update
    suspend fun updateNotification(notification: NotificationQueue)

    /**
     * Bulk update notifications to processing status
     */
    @Query("""
        UPDATE notification_queue
        SET status = 'PROCESSING'
        WHERE id IN (:ids)
    """)
    suspend fun markNotificationsAsProcessing(ids: List<Long>)

    /**
     * Mark notification as sent
     */
    @Query("""
        UPDATE notification_queue
        SET status = 'SENT', lastAttemptAt = :timestamp
        WHERE id = :id
    """)
    suspend fun markNotificationAsSent(id: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Mark notification as failed
     */
    @Query("""
        UPDATE notification_queue
        SET status = 'FAILED', retryCount = retryCount + 1,
            lastAttemptAt = :timestamp, errorMessage = :errorMessage
        WHERE id = :id
    """)
    suspend fun markNotificationAsFailed(
        id: Long,
        errorMessage: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Get queue statistics
     */
    @Query("""
        SELECT status, COUNT(*) as count
        FROM notification_queue
        GROUP BY status
    """)
    suspend fun getQueueStats(): List<QueueStatusCount>

    /**
     * Get queue statistics with real-time updates
     */
    @Query("""
        SELECT status, COUNT(*) as count
        FROM notification_queue
        GROUP BY status
    """)
    fun getQueueStatsFlow(): Flow<List<QueueStatusCount>>

    /**
     * Get count by status with real-time updates
     */
    @Query("""
        SELECT COUNT(*) 
        FROM notification_queue 
        WHERE status = :status
    """)
    fun getCountByStatusFlow(status: String): Flow<Int>

    /**
     * Get recent notifications with real-time updates
     */
    @Query("""
        SELECT * FROM notification_queue
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getRecentNotificationsFlow(limit: Int = 100, offset: Int = 0): Flow<List<NotificationQueue>>

    /**
     * Clean up old sent notifications to prevent database bloat
     * Keep only recent successful sends for history
     */
    @Query("""
        DELETE FROM notification_queue
        WHERE status = 'SENT'
        AND lastAttemptAt < :cutoffTime
    """)
    suspend fun cleanupOldSentNotifications(cutoffTime: Long)

    /**
     * Clean up permanently failed notifications after a longer period
     */
    @Query("""
        DELETE FROM notification_queue
        WHERE status = 'FAILED'
        AND retryCount >= :maxRetries
        AND lastAttemptAt < :cutoffTime
    """)
    suspend fun cleanupFailedNotifications(maxRetries: Int = 3, cutoffTime: Long)

    /**
     * Get total queue size
     */
    @Query("SELECT COUNT(*) FROM notification_queue WHERE status IN ('PENDING', 'PROCESSING')")
    suspend fun getQueueSize(): Int

    /**
     * Watch queue size changes for UI updates
     */
    @Query("SELECT COUNT(*) FROM notification_queue WHERE status IN ('PENDING', 'PROCESSING')")
    fun observeQueueSize(): Flow<Int>

    /**
     * Get recent queue history for debugging
     */
    @Query("""
        SELECT * FROM notification_queue
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecentNotifications(limit: Int = 100, offset: Int = 0): List<NotificationQueue>

    /**
     * Get lightweight pending notifications (only essential fields for memory optimization)
     */
    @Query("""
        SELECT id, packageName, appName, title, status, retryCount
        FROM notification_queue
        WHERE status = 'PENDING'
        ORDER BY createdAt ASC
        LIMIT :batchSize
    """)
    suspend fun getPendingNotificationsLite(batchSize: Int = 50): List<NotificationQueueLite>

    /**
     * Get memory-efficient queue count by status
     */
    @Query("""
        SELECT COUNT(*) FROM notification_queue
        WHERE status = :status
    """)
    suspend fun getCountByStatus(status: String): Int

    /**
     * Batch delete old processed notifications to prevent database bloat
     */
    @Query("""
        DELETE FROM notification_queue
        WHERE id IN (
            SELECT id FROM notification_queue
            WHERE status IN ('SENT', 'FAILED')
            AND lastAttemptAt < :cutoffTime
            ORDER BY lastAttemptAt ASC
            LIMIT :batchSize
        )
    """)
    suspend fun batchDeleteOldNotifications(cutoffTime: Long, batchSize: Int = 100): Int

    /**
     * Get database size optimization stats
     */
    @Query("""
        SELECT
            status,
            COUNT(*) as count,
            AVG(LENGTH(title) + LENGTH(text) + LENGTH(appName)) as avgSize
        FROM notification_queue
        GROUP BY status
    """)
    suspend fun getDatabaseOptimizationStats(): List<DatabaseOptimizationStat>

    /**
     * Get notifications within a time range for statistics
     */
    @Query("""
        SELECT * FROM notification_queue
        WHERE createdAt >= :startTime AND createdAt <= :endTime
        ORDER BY createdAt DESC
    """)
    suspend fun getNotificationsInTimeRange(startTime: Long, endTime: Long): List<NotificationQueue>

    /**
     * Observe queue changes for real-time statistics
     */
    @Query("SELECT COUNT(*) FROM notification_queue")
    fun observeQueueChanges(): Flow<Int>
}
