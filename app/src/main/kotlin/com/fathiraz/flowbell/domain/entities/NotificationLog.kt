package com.fathiraz.flowbell.domain.entities

import kotlinx.serialization.Serializable

/**
 * Data class representing a notification log entry
 */
@Serializable
data class NotificationLog(
    val id: String,
    val appName: String,
    val packageName: String,
    val title: String, // for compatibility
    val text: String, // for compatibility
    val notificationTitle: String,
    val notificationText: String,
    val timestamp: Long,
    val priority: Int,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val status: NotificationQueueStatus,
    val retryCount: Int,
    val lastAttemptAt: Long?,
    val errorMessage: String?,
    val httpDetails: HttpRequestResponseDetails?,
    val iconUri: String? = null,
    val largeIconUri: String? = null,
    val createdAt: Long = timestamp
)

