package com.fathiraz.flowbell.domain.entities

/**
 * Domain entity representing a webhook configuration.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class Webhook(
    val id: String,
    val url: String,
    val name: String,
    val isEnabled: Boolean = true,
    val isSecure: Boolean = true, // Only HTTPS URLs
    val retryCount: Int = 1,
    val timeoutSeconds: Int = 30
)

/**
 * Domain entity representing a webhook delivery result.
 */
data class WebhookDeliveryResult(
    val webhookId: String,
    val notificationId: String,
    val success: Boolean,
    val timestamp: Long,
    val errorMessage: String? = null,
    val responseCode: Int? = null
)