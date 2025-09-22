package com.fathiraz.flowbell.data.services

import android.content.Context
import com.fathiraz.flowbell.domain.entities.NotificationData
import com.fathiraz.flowbell.core.utils.ImageProcessorUtils
import com.fathiraz.flowbell.data.mappers.NotificationMapper
import com.fathiraz.flowbell.data.remote.dto.WebhookPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/**
 * Service responsible for processing notifications and preparing webhook payloads.
 * Handles the complete flow from notification data to webhook-ready JSON.
 */
class WebhookService(
    private val context: Context,
    private val notificationMapper: NotificationMapper,
    private val imageProcessor: ImageProcessorUtils
) {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Processes notification data and creates a webhook payload
     */
    suspend fun processNotification(notificationData: NotificationData): WebhookPayload? {
        return try {
            Timber.d("Processing notification: '${notificationData.title}' from ${notificationData.appName}")
            
            // Map notification data to webhook payload
            val payload = notificationMapper.mapToWebhookPayload(notificationData)
            
            // Process images if needed
            val enhancedPayload = enhancePayloadWithImages(payload, notificationData)
            
            // Add security signature
            val finalPayload = addSecuritySignature(enhancedPayload)
            
            Timber.d("Successfully processed notification payload with ID: ${finalPayload.id}")
            finalPayload
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to process notification: ${notificationData.title}")
            null
        }
    }
    
    /**
     * Processes notification and returns JSON string ready for webhook
     */
    suspend fun processNotificationToJson(notificationData: NotificationData): String? {
        return try {
            val payload = processNotification(notificationData)
            payload?.let { notificationMapper.toJson(it) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to process notification to JSON")
            null
        }
    }
    
    /**
     * Enhances payload with processed images
     */
    private suspend fun enhancePayloadWithImages(
        payload: WebhookPayload,
        notificationData: NotificationData
    ): WebhookPayload {
        return try {
            // For now, we'll keep the existing URI approach
            // In the future, we could add Base64 processing here
            payload
        } catch (e: Exception) {
            Timber.w(e, "Failed to enhance payload with images, using original")
            payload
        }
    }
    
    /**
     * Adds security signature to the payload
     */
    private fun addSecuritySignature(payload: WebhookPayload): WebhookPayload {
        return try {
            val signature = generateSignature(payload)
            payload.copy(
                security = payload.security.copy(signature = signature)
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to add security signature, using payload without signature")
            payload
        }
    }
    
    /**
     * Generates HMAC signature for the payload
     */
    private fun generateSignature(payload: WebhookPayload): String? {
        return try {
            // For now, we'll use a simple approach
            // In production, you'd want to use a proper secret key
            val secretKey = getWebhookSecretKey()
            if (secretKey.isNullOrBlank()) {
                Timber.w("No webhook secret key configured, skipping signature")
                return null
            }
            
            val payloadJson = notificationMapper.toJson(payload)
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
            mac.init(secretKeySpec)
            
            val signature = mac.doFinal(payloadJson.toByteArray())
            val signatureHex = signature.joinToString("") { "%02x".format(it) }
            
            Timber.d("Generated HMAC signature for payload")
            signatureHex
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate signature")
            null
        }
    }
    
    /**
     * Gets the webhook secret key for signing
     */
    private fun getWebhookSecretKey(): String? {
        // For now, return null (no signature)
        // In production, this would come from secure storage
        return null
    }
    
    /**
     * Validates that a payload is ready for webhook delivery
     */
    fun validatePayload(payload: WebhookPayload): Boolean {
        return try {
            // Check required fields
            payload.id.isNotBlank() &&
            payload.timestamp.isNotBlank() &&
            payload.app.packageName.isNotBlank() &&
            payload.app.name.isNotBlank() &&
            payload.notification.title.isNotBlank() &&
            payload.device.id.isNotBlank() &&
            payload.security.nonce.isNotBlank()
        } catch (e: Exception) {
            Timber.e(e, "Failed to validate payload")
            false
        }
    }
    
    /**
     * Gets payload size in bytes
     */
    fun getPayloadSize(payload: WebhookPayload): Int {
        return try {
            val json = notificationMapper.toJson(payload)
            json.toByteArray().size
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate payload size")
            0
        }
    }
    
    /**
     * Checks if payload is within size limits
     */
    fun isPayloadWithinLimits(payload: WebhookPayload, maxSizeKB: Int = 1024): Boolean {
        val sizeBytes = getPayloadSize(payload)
        val sizeKB = sizeBytes / 1024
        return sizeKB <= maxSizeKB
    }
}
