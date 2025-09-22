package com.fathiraz.flowbell.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.fathiraz.flowbell.domain.entities.HttpRequestResponseDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/**
 * Utility class for making HTTP requests with OkHttp and Chucker integration
 */
class HttpRequestUtils(private val context: Context) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Increased timeout
            .readTimeout(45, TimeUnit.SECONDS)     // Increased timeout
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)        // Enable retry on connection failure
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(context))
                    .maxContentLength(250000L)
                    .redactHeaders(emptySet())
                    .alwaysReadResponseBody(false)
                    .build()
            )
            .build()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun checkHostResolution(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val parsedUrl = URL(url)
            val hostname = parsedUrl.host
            Timber.d("🔍 Attempting to resolve hostname: $hostname")

            // Check network connectivity details
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            Timber.d("📶 Network info: network=$network, capabilities=$networkCapabilities")
            Timber.d("📡 Has WiFi: ${networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
            Timber.d("📱 Has Cellular: ${networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
            Timber.d("🌐 Has Internet: ${networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
            Timber.d("✅ Validated: ${networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")

            // Try multiple DNS resolution methods
            try {
                val address = InetAddress.getByName(hostname)
                val resolvedIp = address.hostAddress
                Timber.d("✅ Successfully resolved $hostname to $resolvedIp using InetAddress.getByName()")
                return@withContext null // Success
            } catch (e: Exception) {
                Timber.e("❌ InetAddress.getByName() failed: ${e.message}")

                // Try alternative DNS resolution
                try {
                    val allAddresses = InetAddress.getAllByName(hostname)
                    if (allAddresses.isNotEmpty()) {
                        val resolvedIps = allAddresses.map { it.hostAddress }.joinToString(", ")
                        Timber.d("✅ Successfully resolved $hostname to IPs: $resolvedIps using getAllByName()")
                        return@withContext null // Success
                    }
                } catch (e2: Exception) {
                    Timber.e("❌ InetAddress.getAllByName() also failed: ${e2.message}")
                }

                // Log DNS server information
                try {
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    val dnsServers = linkProperties?.dnsServers
                    Timber.d("🔧 DNS Servers: ${dnsServers?.map { it.hostAddress }?.joinToString(", ")}")

                } catch (e3: Exception) {
                    Timber.e("❌ Failed to get DNS server info: ${e3.message}")
                }

                throw e // Re-throw original exception
            }
        } catch (e: Exception) {
            val parsedUrl = URL(url)
            val hostname = parsedUrl.host
            val errorMsg = when {
                e.message?.contains("No address associated with hostname") == true -> {
                    "DNS lookup failed: The hostname '$hostname' cannot be resolved. This could be due to:\n" +
                    "• No internet connection\n" +
                    "• DNS server issues\n" +
                    "• Hostname doesn't exist\n" +
                    "• Corporate firewall/proxy blocking DNS\n" +
                    "Error: ${e.message}"
                }
                e.message?.contains("Unable to resolve host") == true -> {
                    "Network resolution failed: Cannot reach DNS servers to resolve '$hostname'.\n" +
                    "Please check your internet connection and DNS settings.\n" +
                    "Error: ${e.message}"
                }
                else -> {
                    "DNS Resolution failed for hostname '$hostname': ${e.message}"
                }
            }
            Timber.e(e, errorMsg)
            errorMsg
        }
    }

    suspend fun testWebhook(url: String): WebhookResult = withContext(Dispatchers.IO) {
        try {
            // Check network connectivity first
            if (!isNetworkAvailable()) {
                return@withContext WebhookResult.Error("No internet connection. Please check your network settings.")
            }

            Timber.d("Testing webhook URL: $url")

            // Check hostname resolution before attempting request
            val dnsError = checkHostResolution(url)
            if (dnsError != null) {
                return@withContext WebhookResult.Error(dnsError)
            }

            // Create payload matching exact production format (without test field)
            val testPayload = """
                {
                  "id": "${java.util.UUID.randomUUID()}",
                  "timestamp": "${java.time.Instant.now()}",
                  "app": {
                    "packageName": "com.fathiraz.flowbell",
                    "name": "FlowBell Test",
                    "version": "1.0.0"
                  },
                  "notification": {
                    "title": "FlowBell Webhook Test",
                    "text": "This is a test notification from FlowBell webhook system",
                    "subText": null,
                    "priority": "normal",
                    "isOngoing": false,
                    "isClearable": true,
                    "category": null
                  },
                  "media": {
                    "iconUri": "icon://2131230899",
                    "largeIconUri": null,
                    "iconBase64": null,
                    "largeIconBase64": null
                  },
                  "device": {
                    "id": "${android.os.Build.FINGERPRINT}",
                    "platform": "android",
                    "version": "${android.os.Build.VERSION.RELEASE}",
                    "model": "${android.os.Build.MODEL}",
                    "manufacturer": "${android.os.Build.MANUFACTURER}"
                  },
                  "security": {
                    "signature": null,
                    "nonce": "${java.util.UUID.randomUUID().toString().replace("-", "")}",
                    "algorithm": "HMAC-SHA256"
                  }
                }
            """.trimIndent()

            val response = sendWebhook(url, testPayload)

            if (response.responseCode in 200..299) {
                WebhookResult.Success("HTTP ${response.responseCode}: Webhook test successful")
            } else {
                WebhookResult.Error("HTTP ${response.responseCode}: ${response.responseBody ?: "Unknown error"}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Webhook test failed")
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> {
                    "Network error: Cannot resolve hostname. Please check your internet connection and URL."
                }
                e.message?.contains("timeout") == true || e.message?.contains("TIMEOUT") == true -> {
                    "Request timeout: The server is taking too long to respond. Please try again."
                }
                e.message?.contains("Connection refused") == true -> {
                    "Connection refused: The server is not accepting connections on this URL."
                }
                e.message?.contains("No address associated with hostname") == true -> {
                    "DNS error: Cannot find the server. Please verify the URL is correct."
                }
                e.message?.contains("SSL") == true || e.message?.contains("TLS") == true -> {
                    "SSL/TLS error: Secure connection failed. Please check if the URL supports HTTPS."
                }
                else -> {
                    "Connection error: ${e.message ?: "Unknown network error occurred"}"
                }
            }
            WebhookResult.Error(errorMessage, e)
        }
    }

    suspend fun sendWebhook(
        url: String,
        payload: String,
        headers: Map<String, String> = emptyMap(),
        method: String = "POST"
    ): HttpRequestResponseDetails = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Check hostname resolution for production webhooks too
            val dnsError = checkHostResolution(url)
            if (dnsError != null) {
                Timber.e("DNS resolution failed for webhook: $dnsError")
                return@withContext HttpRequestResponseDetails(
                    requestMethod = method,
                    requestUrl = url,
                    requestHeaders = headers,
                    requestBody = payload,
                    responseCode = -1,
                    responseHeaders = emptyMap(),
                    responseBody = "DNS Error: $dnsError",
                    timestamp = startTime,
                    duration = System.currentTimeMillis() - startTime
                )
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(url)
                .method(method, if (method == "POST" || method == "PUT") requestBody else null)
                .header("Content-Type", "application/json")
                .header("User-Agent", "FlowBell/1.0")

            // Add custom headers
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            val request = requestBuilder.build()

            var responseCode = -1
            var responseHeaders = emptyMap<String, String>()
            var responseBody: String? = null

            val responseTime = measureTimeMillis {
                okHttpClient.newCall(request).execute().use { response ->
                    responseCode = response.code
                    responseHeaders = response.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
                    responseBody = response.body?.string()
                }
            }

            HttpRequestResponseDetails(
                requestMethod = method,
                requestUrl = url,
                requestHeaders = headers + mapOf(
                    "Content-Type" to "application/json",
                    "User-Agent" to "FlowBell/1.0"
                ),
                requestBody = payload,
                responseCode = responseCode,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                timestamp = startTime,
                duration = responseTime
            )

        } catch (e: Exception) {
            Timber.e(e, "HTTP request failed: $url")

            HttpRequestResponseDetails(
                requestMethod = method,
                requestUrl = url,
                requestHeaders = headers,
                requestBody = payload,
                responseCode = -1,
                responseHeaders = emptyMap(),
                responseBody = "Error: ${e.message}",
                timestamp = startTime,
                duration = System.currentTimeMillis() - startTime
            )
        }
    }
}