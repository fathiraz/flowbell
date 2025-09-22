package com.fathiraz.flowbell.domain.usecases

import com.fathiraz.flowbell.domain.entities.Notification
import com.fathiraz.flowbell.domain.entities.Webhook
import com.fathiraz.flowbell.domain.entities.WebhookDeliveryResult
import com.fathiraz.flowbell.domain.repositories.WebhookRepository
import timber.log.Timber

/**
 * Use case for sending notifications to webhooks.
 * This use case encapsulates the business logic for webhook delivery.
 */
class SendWebhookUseCase(
    private val webhookRepository: WebhookRepository
) {
    
    /**
     * Send notification to a specific webhook.
     */
    suspend operator fun invoke(
        webhook: Webhook,
        notification: Notification
    ): Result<WebhookDeliveryResult> {
        Timber.d("SendWebhookUseCase: Sending notification ${notification.id} to webhook ${webhook.id}")
        
        return try {
            val result = webhookRepository.sendNotificationToWebhook(webhook, notification)
            Timber.d("SendWebhookUseCase: Webhook delivery result: ${result.getOrNull()?.success}")
            result
        } catch (e: Exception) {
            Timber.e(e, "SendWebhookUseCase: Failed to send webhook")
            Result.failure(e)
        }
    }
    
    /**
     * Send notification to all enabled webhooks.
     */
    suspend fun sendToAllEnabledWebhooks(notification: Notification): List<Result<WebhookDeliveryResult>> {
        Timber.d("SendWebhookUseCase: Sending notification ${notification.id} to all enabled webhooks")
        
        return try {
            val enabledWebhooks = webhookRepository.getEnabledWebhooks()
            val webhooks = mutableListOf<Webhook>()
            
            enabledWebhooks.collect { webhookList ->
                webhooks.addAll(webhookList)
            }
            
            webhooks.map { webhook ->
                invoke(webhook, notification)
            }
        } catch (e: Exception) {
            Timber.e(e, "SendWebhookUseCase: Failed to send to all webhooks")
            listOf(Result.failure(e))
        }
    }
}