package com.fathiraz.flowbell.data.local.database.dao

import com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus

/**
 * Database optimization statistics
 */
data class DatabaseOptimizationStat(
    val status: NotificationQueueStatus,
    val count: Int,
    val avgSize: Double
)