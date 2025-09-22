package com.fathiraz.flowbell.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * Database entity for queuing notifications for batch processing
 * Optimized with indices for common query patterns
 */
@Entity(
    tableName = "notification_queue",
    indices = [
        Index(value = ["createdAt"], name = "idx_created_at"),  // For ORDER BY createdAt DESC
        Index(value = ["status"], name = "idx_status"),        // For WHERE status = ?
        Index(value = ["status", "createdAt"], name = "idx_status_created_at"), // Composite for filtering + sorting
        Index(value = ["appName"], name = "idx_app_name"),     // For app name search
        Index(value = ["packageName"], name = "idx_package_name") // For package search
    ]
)
@Serializable
data class NotificationQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val iconUri: String? = null,
    val largeIconUri: String? = null,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val priority: Int = android.app.Notification.PRIORITY_DEFAULT,
    val status: NotificationQueueStatus = NotificationQueueStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null,
    // HTTP Details
    val httpUrl: String? = null,
    val httpMethod: String? = null,
    val httpResponseCode: Int? = null,
    val httpResponseBody: String? = null,
    val httpDuration: Long? = null
)

/**
 * Status of notifications in the queue
 */
enum class NotificationQueueStatus {
    PENDING,    // Waiting to be processed
    PROCESSING, // Currently being processed
    SENT,       // Successfully sent
    FAILED      // Failed after retries
}