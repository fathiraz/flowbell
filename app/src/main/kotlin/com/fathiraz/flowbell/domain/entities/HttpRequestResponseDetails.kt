package com.fathiraz.flowbell.domain.entities

import kotlinx.serialization.Serializable

/**
 * Data class representing HTTP request and response details for webhook calls
 */
@Serializable
data class HttpRequestResponseDetails(
    val requestMethod: String,
    val requestUrl: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String? = null,
    val responseCode: Int,
    val responseHeaders: Map<String, String>,
    val responseBody: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0L
)
