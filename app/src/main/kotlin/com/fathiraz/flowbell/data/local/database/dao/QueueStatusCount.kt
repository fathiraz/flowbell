package com.fathiraz.flowbell.data.local.database.dao

import com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus

/**
 * Data class for queue statistics
 */
data class QueueStatusCount(
    val status: NotificationQueueStatus,
    val count: Int
)