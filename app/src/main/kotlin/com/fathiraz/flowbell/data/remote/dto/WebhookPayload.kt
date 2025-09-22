package com.fathiraz.flowbell.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Represents the standardized webhook payload structure for notification forwarding.
 * This payload is sent to the configured webhook URL when a notification is received.
 */
@Serializable
data class WebhookPayload(
    @SerialName("id")
    val id: String,
    
    @SerialName("timestamp")
    val timestamp: String, // ISO 8601 format
    
    @SerialName("app")
    val app: AppInfoPayload,
    
    @SerialName("notification")
    val notification: NotificationDetailsPayload,
    
    @SerialName("media")
    val media: MediaPayload,
    
    @SerialName("device")
    val device: DeviceInfoPayload,
    
    @SerialName("security")
    val security: SecurityPayload
)

@Serializable
data class AppInfoPayload(
    @SerialName("packageName")
    val packageName: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("version")
    val version: String? = null
)

@Serializable
data class NotificationDetailsPayload(
    @SerialName("title")
    val title: String,
    
    @SerialName("text")
    val text: String,
    
    @SerialName("subText")
    val subText: String? = null,
    
    @SerialName("priority")
    val priority: String, // "high", "normal", "low"
    
    @SerialName("isOngoing")
    val isOngoing: Boolean,
    
    @SerialName("isClearable")
    val isClearable: Boolean,
    
    @SerialName("category")
    val category: String? = null
)

@Serializable
data class MediaPayload(
    @SerialName("iconUri")
    val iconUri: String? = null,
    
    @SerialName("largeIconUri")
    val largeIconUri: String? = null,
    
    @SerialName("iconBase64")
    val iconBase64: String? = null,
    
    @SerialName("largeIconBase64")
    val largeIconBase64: String? = null
)

@Serializable
data class DeviceInfoPayload(
    @SerialName("id")
    val id: String,
    
    @SerialName("platform")
    val platform: String = "android",
    
    @SerialName("version")
    val version: String,
    
    @SerialName("model")
    val model: String? = null,
    
    @SerialName("manufacturer")
    val manufacturer: String? = null
)

@Serializable
data class SecurityPayload(
    @SerialName("signature")
    val signature: String? = null,
    
    @SerialName("nonce")
    val nonce: String,
    
    @SerialName("algorithm")
    val algorithm: String = "HMAC-SHA256"
)
