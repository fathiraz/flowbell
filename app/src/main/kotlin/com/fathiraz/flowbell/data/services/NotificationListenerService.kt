package com.fathiraz.flowbell

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.fathiraz.flowbell.data.local.database.repository.AppPreferencesRepository
import com.fathiraz.flowbell.data.mappers.NotificationMapper
import com.fathiraz.flowbell.domain.repositories.NotificationQueueRepository
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.fathiraz.flowbell.core.utils.ImageProcessorUtils
import com.fathiraz.flowbell.core.utils.HttpRequestUtils
import com.fathiraz.flowbell.core.utils.NotificationWorkManager
import com.fathiraz.flowbell.core.utils.BatteryOptimizerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentLinkedQueue

data class NotificationData(
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val iconUri: String? = null,
    val largeIconUri: String? = null,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val priority: Int = Notification.PRIORITY_DEFAULT
)

class NotificationListenerService : NotificationListenerService(), KoinComponent {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  // Inject dependencies using Koin
  private val appPreferencesRepository: AppPreferencesRepository by inject()
  private val userPreferencesRepository: UserPreferencesRepository by inject()
  private val notificationQueueRepository: NotificationQueueRepository by inject()
  private val httpRequest: HttpRequestUtils by inject()

  // Late init for components that need context
  private lateinit var notificationMapper: NotificationMapper
  private lateinit var imageProcessor: ImageProcessorUtils
  private lateinit var notificationWorkManager: NotificationWorkManager
  private lateinit var batteryOptimizer: BatteryOptimizerUtils
  
  // Track processed notifications to prevent duplicates
  private val processedNotifications = mutableSetOf<String>()

  // Batch processing for webhooks
  private val notificationBatch = ConcurrentLinkedQueue<NotificationData>()
  private var batchProcessingJob: kotlinx.coroutines.Job? = null

  override fun onCreate() {
    super.onCreate()
    Timber.d("🚀 NotificationListenerService onCreate() called")
    try {
      initializeServices()
      Timber.d("✅ NotificationListenerService initialized successfully")
    } catch (e: Exception) {
      Timber.e(e, "❌ Failed to initialize NotificationListenerService")
    }
  }

  private fun initializeServices() {
    Timber.d("🔧 Initializing NotificationListenerService components...")

    try {
      // Initialize context-dependent components
      notificationMapper = NotificationMapper
      Timber.d("✅ NotificationMapper initialized")

      imageProcessor = ImageProcessorUtils(this)
      Timber.d("✅ ImageProcessor initialized")

      // Initialize NotificationWorkManager
      notificationWorkManager = NotificationWorkManager(this)
      notificationWorkManager.initialize()
      Timber.d("✅ NotificationWorkManager initialized")

      // Initialize BatteryOptimizer
      batteryOptimizer = BatteryOptimizerUtils(this)
      Timber.d("✅ BatteryOptimizer initialized")

      // Koin-injected dependencies are automatically available
      Timber.d("✅ Koin dependencies injected: AppPreferencesRepository, UserPreferencesRepository, NotificationQueueRepository, HttpRequestUtils")

      // Start batch processing
      startBatchProcessing()

    } catch (e: Exception) {
      Timber.e(e, "❌ Error during service initialization")
      throw e
    }
  }

  /**
   * Start batch processing for webhooks - processes batches every 5 seconds
   */
  private fun startBatchProcessing() {
    batchProcessingJob?.cancel() // Cancel any existing job

    batchProcessingJob = serviceScope.launch {
      Timber.i("🔄 Starting batch processing - webhooks will be sent every 5 seconds")

      while (true) {
        try {
          delay(5000) // Wait 5 seconds

          if (notificationBatch.isNotEmpty()) {
            processBatch()
          }
        } catch (e: Exception) {
          Timber.e(e, "❌ Error in batch processing loop")
          // Continue the loop even if there's an error
        }
      }
    }
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    super.onNotificationPosted(sbn)
    
    // Create a unique identifier for this notification
    val notificationId = "${sbn.packageName}:${sbn.id}:${sbn.postTime}"
    
    Timber.d("🔔 onNotificationPosted called for package: ${sbn.packageName} (ID: $notificationId)")
    
    // Check if we've already processed this notification
    if (processedNotifications.contains(notificationId)) {
      Timber.d("🔄 Duplicate notification detected - ignoring: $notificationId")
      return
    }
    
    // Add to processed set
    processedNotifications.add(notificationId)
    
    // Clean up old entries (keep only last 100)
    if (processedNotifications.size > 100) {
      val toRemove = processedNotifications.take(processedNotifications.size - 100)
      processedNotifications.removeAll(toRemove)
    }
    
    serviceScope.launch {
      try {
        val notification = sbn.notification
        val data = extractNotificationData(sbn, notification)
        
        Timber.d("📱 Notification received: '${data.title}' from ${data.appName} (${data.packageName})")
        
        // Check if app is enabled for forwarding with detailed logging
        Timber.d("🔍 Checking if app ${data.packageName} is enabled for forwarding...")
        Timber.d("📊 Repository instance: ${appPreferencesRepository::class.java.simpleName}")

        val isAppEnabled = appPreferencesRepository.getForwardingStatus(data.packageName)
        Timber.d("🔍 Forwarding status for ${data.packageName}: $isAppEnabled")

        if (!isAppEnabled) {
          Timber.w("❌ App ${data.packageName} is disabled for forwarding - ignoring notification")
          Timber.d("💡 To enable: Go to Apps screen and toggle ${data.appName} ON")
          return@launch
        }

        Timber.i("✅ App ${data.packageName} is enabled for forwarding - adding to batch")
        Timber.d("📨 Notification details: title='${data.title}', text='${data.text}'")

        // Add to batch for processing
        addToBatch(data)
        
      } catch (e: Exception) {
        Timber.e(e, "❌ Error processing notification from ${sbn.packageName}")
      }
    }
  }

  private fun extractNotificationData(sbn: StatusBarNotification, notification: Notification): NotificationData {
    val packageName = sbn.packageName
    val extras = notification.extras
    
    // Extract basic text data
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
    
    // Combine text and subtext if available
    val fullText = if (subText.isNotEmpty()) "$text - $subText" else text
    
    // Get app name
    val appName = getAppName(packageName)
    
    // Extract icon information
    val iconUri = extractIconUri(notification)
    val largeIconUri = extractLargeIconUri(notification)
    
    // Extract notification properties
    val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
    val isClearable = (notification.flags and Notification.FLAG_NO_CLEAR) == 0
    val priority = notification.priority

    return NotificationData(
      packageName = packageName,
      appName = appName,
      title = title,
      text = fullText,
      timestamp = sbn.postTime,
      iconUri = iconUri,
      largeIconUri = largeIconUri,
      isOngoing = isOngoing,
      isClearable = isClearable,
      priority = priority
    )
  }
  
  private fun getAppName(packageName: String): String {
    return try {
      val packageManager = packageManager
      val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
      packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
      Timber.w("Could not find app name for package: $packageName")
      packageName
    }
  }
  
  private fun extractIconUri(notification: Notification): String? {
    return try {
      // For now, we'll store the icon resource ID
      // In a full implementation, you might want to convert this to a URI or base64
      if (notification.icon != 0) {
        "icon://${notification.icon}"
      } else null
    } catch (e: Exception) {
      Timber.w(e, "Could not extract notification icon")
      null
    }
  }
  
  private fun extractLargeIconUri(notification: Notification): String? {
    return try {
      // For now, we'll store a placeholder
      // In a full implementation, you might want to save the large icon as a file
      if (notification.largeIcon != null) {
        "large_icon://${notification.largeIcon.hashCode()}"
      } else null
    } catch (e: Exception) {
      Timber.w(e, "Could not extract large notification icon")
      null
    }
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    Timber.d("🔌 Notification listener disconnected")
  }

  override fun onListenerConnected() {
    super.onListenerConnected()
    Timber.i("🔌 Notification listener connected successfully")

    // Log service status for debugging
    serviceScope.launch {
      try {
        Timber.i("🔍 Service status check:")
        Timber.d("✅ AppPreferencesRepository: ${appPreferencesRepository::class.java.simpleName}")
        Timber.d("✅ UserPreferencesRepository: ${userPreferencesRepository::class.java.simpleName}")
        Timber.d("✅ NotificationQueueRepository: ${notificationQueueRepository::class.java.simpleName}")
        Timber.d("✅ HttpRequestUtils: ${httpRequest::class.java.simpleName}")

        // Test webhook URL availability
        val prefs = userPreferencesRepository.getUserPreferences().first()
        Timber.d("🔗 Webhook URL configured: ${if (prefs.webhookUrl.isNotEmpty()) "YES" else "NO"}")

        Timber.i("🚀 NotificationListenerService is ready to process notifications")
      } catch (e: Exception) {
        Timber.e(e, "❌ Error during service status check")
      }
    }
  }

  /**
   * Add notification to batch for processing
   */
  private fun addToBatch(data: NotificationData) {
    notificationBatch.offer(data)
    val batchSize = notificationBatch.size
    Timber.d("📦 Added notification to batch: ${data.appName} - Batch size: $batchSize")
  }

  /**
   * Process the current batch of notifications
   */
  private suspend fun processBatch() {
    val batchList = mutableListOf<NotificationData>()

    // Drain the queue into a list for processing
    while (true) {
      val notification = notificationBatch.poll() ?: break
      batchList.add(notification)
    }

    if (batchList.isEmpty()) {
      return
    }

    Timber.i("🚀 Processing batch of ${batchList.size} notifications")

    try {
      // Get webhook URL once for the batch
      val userPreferences = userPreferencesRepository.getUserPreferences()
      val preferences = userPreferences.first()
      val webhookUrl = preferences.webhookUrl

      if (webhookUrl.isEmpty()) {
        Timber.w("⚠️ No webhook URL configured - skipping batch of ${batchList.size} notifications")

        // Still save to database for statistics/history
        batchList.forEach { data ->
          saveNotificationToHistory(data, NotificationQueueStatus.FAILED, "No webhook URL configured")
        }
        return
      }

      // Process each notification in the batch
      batchList.forEach { data ->
        sendSingleWebhook(data, webhookUrl)
      }

      Timber.i("✅ Completed processing batch of ${batchList.size} notifications")

    } catch (e: Exception) {
      Timber.e(e, "❌ Error processing batch")

      // Save failed notifications to database
      batchList.forEach { data ->
        saveNotificationToHistory(data, NotificationQueueStatus.FAILED, "Batch processing error: ${e.message}")
      }
    }
  }

  /**
   * Send a single webhook within a batch
   */
  private suspend fun sendSingleWebhook(data: NotificationData, webhookUrl: String) {
    try {
      Timber.d("📡 Sending webhook for: ${data.appName} - ${data.title}")

      // Create webhook payload
      val payload = createWebhookPayload(data)

      // Send webhook
      val result = httpRequest.sendWebhook(webhookUrl, payload)

      if (result.responseCode in 200..299) {
        Timber.d("✅ Webhook sent: ${data.appName} - HTTP ${result.responseCode}")
        saveNotificationToHistory(data, NotificationQueueStatus.SENT, null, result)
        // Show user notification for successful webhook send
        showWebhookSentNotification(data)
      } else {
        Timber.w("❌ Webhook failed: ${data.appName} - HTTP ${result.responseCode}")
        saveNotificationToHistory(data, NotificationQueueStatus.FAILED, "HTTP ${result.responseCode}: ${result.responseBody}", result)
      }

    } catch (e: Exception) {
      Timber.e(e, "❌ Error sending webhook for: ${data.appName}")
      saveNotificationToHistory(data, NotificationQueueStatus.FAILED, "Exception: ${e.message}")
    }
  }

  
  /**
   * Show user notification when webhook is sent successfully
   */
  private fun showWebhookSentNotification(data: NotificationData, batchSize: Int = 1) {
    try {
      android.util.Log.d("NotificationListenerService", "📢 Showing webhook sent notification for ${data.appName}")
      
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      
      // Create notification channel for webhook status
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          WEBHOOK_CHANNEL_ID,
          "Webhook Status",
          NotificationManager.IMPORTANCE_LOW
        ).apply {
          description = "Notifications about webhook delivery status"
          setShowBadge(false)
          enableVibration(false)
          enableLights(false)
        }
        notificationManager.createNotificationChannel(channel)
      }
      
      val title = if (batchSize > 1) {
        "✅ $batchSize webhooks sent"
      } else {
        "✅ Webhook sent"
      }
      
      val content = if (batchSize > 1) {
        "Successfully forwarded $batchSize notifications including ${data.appName}"
      } else {
        "Successfully forwarded notification from ${data.appName}"
      }
      
      val notification = NotificationCompat.Builder(this, WEBHOOK_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setGroup(WEBHOOK_GROUP_KEY)
        .setAutoCancel(true)
        .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
        .build()
      
      // Use grouped notifications to avoid spam
      notificationManager.notify(WEBHOOK_NOTIFICATION_ID, notification)
      
      android.util.Log.d("NotificationListenerService", "✅ Webhook notification shown: $title")
      
    } catch (e: Exception) {
      android.util.Log.e("NotificationListenerService", "❌ Error showing webhook notification", e)
      Timber.e(e, "Error showing webhook notification")
    }
  }
  
  companion object {
    private const val WEBHOOK_CHANNEL_ID = "webhook_status_channel"
    private const val WEBHOOK_GROUP_KEY = "webhook_group"
    private const val WEBHOOK_NOTIFICATION_ID = 1001
  }

  /**
   * Save notification to history/statistics database
   */
  private suspend fun saveNotificationToHistory(
    data: NotificationData,
    status: NotificationQueueStatus,
    errorMessage: String? = null,
    httpDetails: com.fathiraz.flowbell.domain.entities.HttpRequestResponseDetails? = null
  ) {
    try {
      val notificationLog = convertToNotificationLog(data).copy(
        status = status,
        errorMessage = errorMessage,
        httpDetails = httpDetails,
        lastAttemptAt = System.currentTimeMillis()
      )

      notificationQueueRepository.addToQueue(notificationLog)
      Timber.d("📊 Saved to history: ${data.appName} - Status: $status")

    } catch (e: Exception) {
      Timber.e(e, "❌ Failed to save notification to history: ${data.appName}")
    }
  }

  /**
   * Send webhook directly for real-time notification forwarding
   * @deprecated Use batch processing instead
   */
  private suspend fun sendWebhookDirectly(data: NotificationData) {
    try {
      Timber.i("🚀 Starting webhook send process for: '${data.title}' from ${data.appName}")
      Timber.d("📊 UserPreferencesRepository instance: ${userPreferencesRepository::class.java.simpleName}")

      // Get current webhook URL from preferences
      Timber.d("🔍 Fetching webhook URL from user preferences...")
      val userPreferences = userPreferencesRepository.getUserPreferences()
      val preferences = userPreferences.first() // Take only the first value
      val webhookUrl = preferences.webhookUrl

      Timber.d("🔗 Retrieved webhook URL: ${if (webhookUrl.isNotEmpty()) "[URL_SET]" else "[EMPTY]"}")

      if (webhookUrl.isEmpty()) {
        Timber.w("⚠️ No webhook URL configured - skipping notification")
        Timber.d("💡 To configure: Go to Settings screen and set webhook URL")
        return
      }

      Timber.i("📡 Sending webhook to configured URL for ${data.packageName}")
      Timber.d("📊 HttpRequestUtils instance: ${httpRequest::class.java.simpleName}")

      // Create webhook payload matching production format
      Timber.d("📝 Creating webhook payload...")
      val payload = createWebhookPayload(data)
      Timber.d("📄 Payload created, size: ${payload.length} characters")

      // Send webhook using HttpRequestUtils
      Timber.i("🌐 Sending HTTP request to webhook...")
      val result = httpRequest.sendWebhook(webhookUrl, payload)

      Timber.d("📨 HTTP Response: code=${result.responseCode}, duration=${result.duration}ms")

      if (result.responseCode in 200..299) {
        Timber.i("✅ Webhook sent successfully: HTTP ${result.responseCode} for ${data.packageName}")
        Timber.d("🎯 Success details: ${result.responseBody?.take(100)}")

        // Save to queue for logging/statistics
        val notificationLog = convertToNotificationLog(data).copy(
          status = NotificationQueueStatus.SENT,
          httpDetails = result
        )
        notificationQueueRepository.addToQueue(notificationLog)
        Timber.d("📊 Logged successful webhook to queue")
        // Show user notification for successful webhook send
        showWebhookSentNotification(data)

      } else {
        Timber.e("❌ Webhook failed: HTTP ${result.responseCode} for ${data.packageName}")
        Timber.e("💥 Error details: ${result.responseBody}")

        // Save failed attempt for retry later
        val notificationLog = convertToNotificationLog(data).copy(
          status = NotificationQueueStatus.FAILED,
          errorMessage = "HTTP ${result.responseCode}: ${result.responseBody}",
          httpDetails = result
        )
        notificationQueueRepository.addToQueue(notificationLog)
        Timber.d("📊 Logged failed webhook to queue")
      }
    } catch (e: Exception) {
      Timber.e(e, "❌ Critical error sending webhook for notification: ${data.title} from ${data.packageName}")
      Timber.e("💀 Exception type: ${e::class.java.simpleName}")
      Timber.e("📋 Exception message: ${e.message}")

      // Save error for potential retry
      try {
        val notificationLog = convertToNotificationLog(data).copy(
          status = NotificationQueueStatus.FAILED,
          errorMessage = "Exception: ${e.message}"
        )
        notificationQueueRepository.addToQueue(notificationLog)
        Timber.d("📊 Logged exception to queue")
      } catch (dbError: Exception) {
        Timber.e(dbError, "❌ Also failed to save error to queue - critical system failure")
      }
    }
  }

  /**
   * Create webhook payload in production format
   */
  private fun createWebhookPayload(data: NotificationData): String {
    val deviceId = android.os.Build.FINGERPRINT
    val nonce = java.util.UUID.randomUUID().toString().replace("-", "")

    return """
      {
        "id": "${java.util.UUID.randomUUID()}",
        "timestamp": "${java.time.Instant.ofEpochMilli(data.timestamp)}",
        "app": {
          "packageName": "${data.packageName}",
          "name": "${data.appName}",
          "version": "unknown"
        },
        "notification": {
          "title": "${data.title.replace("\"", "\\\"")}",
          "text": "${data.text.replace("\"", "\\\"")}",
          "subText": null,
          "priority": "${getPriorityString(data.priority)}",
          "isOngoing": ${data.isOngoing},
          "isClearable": ${data.isClearable},
          "category": null
        },
        "media": {
          "iconUri": ${if (data.iconUri != null) "\"${data.iconUri}\"" else "null"},
          "largeIconUri": ${if (data.largeIconUri != null) "\"${data.largeIconUri}\"" else "null"},
          "iconBase64": null,
          "largeIconBase64": null
        },
        "device": {
          "id": "$deviceId",
          "platform": "android",
          "version": "${android.os.Build.VERSION.RELEASE}",
          "model": "${android.os.Build.MODEL}",
          "manufacturer": "${android.os.Build.MANUFACTURER}"
        },
        "security": {
          "signature": null,
          "nonce": "$nonce",
          "algorithm": "HMAC-SHA256"
        }
      }
    """.trimIndent()
  }

  /**
   * Convert notification priority to string
   */
  private fun getPriorityString(priority: Int): String {
    return when (priority) {
      Notification.PRIORITY_MIN -> "min"
      Notification.PRIORITY_LOW -> "low"
      Notification.PRIORITY_DEFAULT -> "normal"
      Notification.PRIORITY_HIGH -> "high"
      Notification.PRIORITY_MAX -> "max"
      else -> "normal"
    }
  }

  /**
   * Debug method to check WorkManager status and force processing
   * This can be called from the UI or through ADB for debugging
   */
  fun debugWorkManagerStatus() {
    try {
      Timber.d("🔍 DEBUG: NotificationListenerService - Checking WorkManager status...")
      notificationWorkManager.debugWorkManagerStatus()
    } catch (e: Exception) {
      Timber.e(e, "❌ Error in debugWorkManagerStatus")
    }
  }

  /**
   * Convert NotificationData to NotificationLog for repository storage
   */
  private fun convertToNotificationLog(data: NotificationData): NotificationLog {
    return NotificationLog(
      id = "${data.packageName}_${data.timestamp}", // Generate unique ID
      appName = data.appName,
      packageName = data.packageName,
      title = data.title,
      text = data.text,
      notificationTitle = data.title,
      notificationText = data.text,
      timestamp = data.timestamp,
      priority = data.priority,
      isOngoing = data.isOngoing,
      isClearable = data.isClearable,
      status = NotificationQueueStatus.PENDING,
      retryCount = 0,
      lastAttemptAt = null,
      errorMessage = null,
      httpDetails = null
    )
  }
}
