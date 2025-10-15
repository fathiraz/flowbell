package com.fathiraz.flowbell.data.local.database.dao

/**
 * Data class for app count query results from the database
 */
data class AppCountResult(
    val packageName: String,
    val appName: String,
    val count: Int
)
