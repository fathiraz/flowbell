package com.fathiraz.flowbell.data.local.database.dao

import com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus

/**
 * Lightweight notification data for memory optimization
 */
data class NotificationQueueLite(
    val id: Long,
    val packageName: String,
    val appName: String,
    val title: String,
    val status: NotificationQueueStatus,
    val retryCount: Int
)