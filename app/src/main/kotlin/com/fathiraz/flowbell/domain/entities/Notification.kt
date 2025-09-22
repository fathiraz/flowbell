package com.fathiraz.flowbell.domain.entities

import java.time.Instant

/**
 * Domain entity representing a notification.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class Notification(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val timestamp: Instant,
    val priority: NotificationPriority,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val category: String? = null,
    val iconUri: String? = null,
    val largeIconUri: String? = null
)

/**
 * Enum representing notification priority levels.
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}