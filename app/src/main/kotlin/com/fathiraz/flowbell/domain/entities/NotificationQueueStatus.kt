package com.fathiraz.flowbell.domain.entities

/**
 * Domain representation of notification queue status
 */
enum class NotificationQueueStatus {
    PENDING,    // Waiting to be processed
    PROCESSING, // Currently being processed
    SENT,       // Successfully sent
    FAILED      // Failed after retries
}