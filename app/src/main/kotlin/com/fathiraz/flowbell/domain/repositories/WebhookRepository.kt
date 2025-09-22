package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.Webhook
import com.fathiraz.flowbell.domain.entities.WebhookDeliveryResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for webhook-related operations.
 * This interface defines the contract for webhook data operations.
 */
interface WebhookRepository {
    
    /**
     * Get all configured webhooks.
     */
    suspend fun getAllWebhooks(): Flow<List<Webhook>>
    
    /**
     * Get enabled webhooks only.
     */
    suspend fun getEnabledWebhooks(): Flow<List<Webhook>>
    
    /**
     * Save or update a webhook.
     */
    suspend fun saveWebhook(webhook: Webhook): Result<Unit>
    
    /**
     * Delete a webhook.
     */
    suspend fun deleteWebhook(webhookId: String): Result<Unit>
    
    /**
     * Get webhook by ID.
     */
    suspend fun getWebhookById(webhookId: String): Result<Webhook>
    
    /**
     * Validate webhook URL.
     */
    suspend fun validateWebhookUrl(url: String): Result<Boolean>
    
    /**
     * Send notification to webhook.
     */
    suspend fun sendNotificationToWebhook(
        webhook: Webhook,
        notification: com.fathiraz.flowbell.domain.entities.Notification
    ): Result<WebhookDeliveryResult>
    
    /**
     * Get webhook delivery history.
     */
    suspend fun getWebhookDeliveryHistory(): Flow<List<WebhookDeliveryResult>>
}
