package com.fathiraz.flowbell.core.utils

/**
 * Sealed class representing webhook operation results
 */
sealed class WebhookResult {
    data class Success(val response: String) : WebhookResult()
    data class Error(val message: String, val cause: Throwable? = null) : WebhookResult()
}