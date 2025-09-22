package com.fathiraz.flowbell.data.mappers

import com.fathiraz.flowbell.data.local.database.entities.NotificationQueue
import com.fathiraz.flowbell.domain.entities.Notification
import com.fathiraz.flowbell.domain.entities.NotificationPriority
import com.fathiraz.flowbell.domain.entities.NotificationData
import com.fathiraz.flowbell.data.remote.dto.WebhookPayload
import com.fathiraz.flowbell.data.remote.dto.AppInfoPayload
import com.fathiraz.flowbell.data.remote.dto.NotificationDetailsPayload
import com.fathiraz.flowbell.data.remote.dto.MediaPayload
import com.fathiraz.flowbell.data.remote.dto.DeviceInfoPayload
import com.fathiraz.flowbell.data.remote.dto.SecurityPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

object NotificationMapper {
    
    /**
     * Convert data NotificationQueue to domain Notification.
     */
    fun toDomain(notificationQueue: NotificationQueue): Notification {
        return Notification(
            id = notificationQueue.id.toString(),
            packageName = notificationQueue.packageName,
            title = notificationQueue.title,
            text = notificationQueue.text,
            subText = null, // NotificationQueue doesn't have subText
            timestamp = java.time.Instant.ofEpochMilli(notificationQueue.timestamp),
            priority = mapPriorityFromInt(notificationQueue.priority),
            isOngoing = notificationQueue.isOngoing,
            isClearable = notificationQueue.isClearable,
            category = null, // NotificationQueue doesn't have category
            iconUri = notificationQueue.iconUri,
            largeIconUri = notificationQueue.largeIconUri
        )
    }
    
    /**
     * Convert domain Notification to data NotificationQueue.
     */
    fun toData(notification: Notification): NotificationQueue {
        return NotificationQueue(
            id = notification.id.toLongOrNull() ?: 0,
            packageName = notification.packageName,
            appName = "", // Will need to be provided separately or derived
            title = notification.title,
            text = notification.text,
            timestamp = notification.timestamp.toEpochMilli(),
            priority = mapPriorityToInt(notification.priority),
            isOngoing = notification.isOngoing,
            isClearable = notification.isClearable,
            iconUri = notification.iconUri,
            largeIconUri = notification.largeIconUri,
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun mapPriorityFromInt(priorityInt: Int): NotificationPriority {
        return when (priorityInt) {
            android.app.Notification.PRIORITY_MAX -> NotificationPriority.URGENT
            android.app.Notification.PRIORITY_HIGH -> NotificationPriority.HIGH
            android.app.Notification.PRIORITY_DEFAULT -> NotificationPriority.NORMAL
            android.app.Notification.PRIORITY_LOW -> NotificationPriority.LOW
            android.app.Notification.PRIORITY_MIN -> NotificationPriority.LOW
            else -> NotificationPriority.NORMAL
        }
    }
    
    private fun mapPriorityToInt(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.URGENT -> android.app.Notification.PRIORITY_MAX
            NotificationPriority.HIGH -> android.app.Notification.PRIORITY_HIGH
            NotificationPriority.NORMAL -> android.app.Notification.PRIORITY_DEFAULT
            NotificationPriority.LOW -> android.app.Notification.PRIORITY_LOW
        }
    }

    /**
     * Map NotificationData to WebhookPayload
     */
    fun mapToWebhookPayload(notificationData: NotificationData): WebhookPayload {
        return WebhookPayload(
            id = UUID.randomUUID().toString(),
            timestamp = Instant.ofEpochMilli(notificationData.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_INSTANT),
            app = AppInfoPayload(
                packageName = notificationData.packageName,
                name = notificationData.appName,
                version = null
            ),
            notification = NotificationDetailsPayload(
                title = notificationData.title,
                text = notificationData.text,
                priority = mapPriorityToString(notificationData.priority),
                isOngoing = notificationData.isOngoing,
                isClearable = notificationData.isClearable
            ),
            media = MediaPayload(
                iconUri = notificationData.iconUri,
                largeIconUri = notificationData.largeIconUri
            ),
            device = DeviceInfoPayload(
                id = "device-id", // Will be populated by service
                version = android.os.Build.VERSION.RELEASE,
                model = android.os.Build.MODEL,
                manufacturer = android.os.Build.MANUFACTURER
            ),
            security = SecurityPayload(
                nonce = UUID.randomUUID().toString()
            )
        )
    }

    private fun mapPriorityToString(priority: Int): String {
        return when (priority) {
            android.app.Notification.PRIORITY_MAX -> "urgent"
            android.app.Notification.PRIORITY_HIGH -> "high"
            android.app.Notification.PRIORITY_DEFAULT -> "normal"
            android.app.Notification.PRIORITY_LOW -> "low"
            android.app.Notification.PRIORITY_MIN -> "low"
            else -> "normal"
        }
    }

    /**
     * Convert WebhookPayload to JSON string
     */
    fun toJson(webhookPayload: WebhookPayload): String {
        return Json.encodeToString(webhookPayload)
    }
}