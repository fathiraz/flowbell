package com.fathiraz.flowbell.data.local.database.dao

/**
 * Data class for daily statistics query results from the database
 */
data class DailyStatsResult(
    val date: String,  // SQLite date format: YYYY-MM-DD
    val totalNotifications: Int,
    val successfulDeliveries: Int,
    val failedDeliveries: Int
)
