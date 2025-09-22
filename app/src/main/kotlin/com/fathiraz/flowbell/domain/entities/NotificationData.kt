package com.fathiraz.flowbell.domain.entities

import android.app.Notification

/**
 * Data class representing notification data for processing
 */
data class NotificationData(
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val iconUri: String? = null,
    val largeIconUri: String? = null,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val priority: Int = Notification.PRIORITY_DEFAULT
)