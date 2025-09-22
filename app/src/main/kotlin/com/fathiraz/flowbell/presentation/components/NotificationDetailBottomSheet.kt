package com.fathiraz.flowbell.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathiraz.flowbell.domain.entities.HttpRequestResponseDetails
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import com.fathiraz.flowbell.presentation.components.StatusBadge
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// Thread-safe cached date formatter
private val dateFormatterCache = ConcurrentHashMap<String, SimpleDateFormat>()

/**
 * Bottom sheet showing detailed notification information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailBottomSheet(
    notificationLog: NotificationLog,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyToClipboard(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        // In a real app, you might show a toast here
        Timber.d("Copied $label to clipboard")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notification Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        HorizontalDivider()

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic notification info
            item {
                NotificationInfoSection(
                    notificationLog = notificationLog,
                    onCopy = { copyToClipboard(it.second, it.first) }
                )
            }

            // Status and timing info
            item {
                StatusAndTimingSection(
                    notificationLog = notificationLog,
                    onCopy = { copyToClipboard(it.second, it.first) }
                )
            }

            // HTTP details if available
            notificationLog.httpDetails?.let { httpDetails ->
                item {
                    HttpRequestSection(
                        httpDetails = httpDetails,
                        onCopy = { copyToClipboard(it.second, it.first) }
                    )
                }

                item {
                    HttpResponseSection(
                        httpDetails = httpDetails,
                        onCopy = { copyToClipboard(it.second, it.first) }
                    )
                }
            }

            // Error details if failed
            if (notificationLog.status == NotificationQueueStatus.FAILED && !notificationLog.errorMessage.isNullOrEmpty()) {
                item {
                    ErrorDetailsSection(
                        errorMessage = notificationLog.errorMessage,
                        onCopy = { copyToClipboard(it.second, it.first) }
                    )
                }
            }
        }
    }
}

/**
 * Basic notification information section
 */
@Composable
private fun NotificationInfoSection(
    notificationLog: NotificationLog,
    onCopy: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailSection(
        title = "Notification Info",
        icon = Icons.Default.Info,
        modifier = modifier
    ) {
        DetailRow(
            label = "App Name",
            value = notificationLog.appName.ifEmpty { "Unknown" },
            onCopy = onCopy
        )

        DetailRow(
            label = "Package",
            value = notificationLog.packageName.ifEmpty { "Unknown" },
            onCopy = onCopy
        )

        DetailRow(
            label = "Title",
            value = notificationLog.notificationTitle?.ifEmpty { "No title" } ?: "No title",
            onCopy = onCopy
        )

        DetailRow(
            label = "Text",
            value = notificationLog.notificationText?.ifEmpty { "No text" } ?: "No text",
            onCopy = onCopy
        )

        DetailRow(
            label = "Priority",
            value = getPriorityLabel(notificationLog.priority),
            onCopy = onCopy
        )

        DetailRow(
            label = "Ongoing",
            value = if (notificationLog.isOngoing) "Yes" else "No",
            onCopy = onCopy
        )

        DetailRow(
            label = "Clearable",
            value = if (notificationLog.isClearable) "Yes" else "No",
            onCopy = onCopy
        )
    }
}

/**
 * Status and timing information section
 */
@Composable
private fun StatusAndTimingSection(
    notificationLog: NotificationLog,
    onCopy: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailSection(
        title = "Status & Timing",
        icon = Icons.Default.AccessTime,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusBadge(status = notificationLog.status)

            IconButton(
                onClick = {
                    onCopy("Status" to notificationLog.status.name)
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy status",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DetailRow(
            label = "Timestamp",
            value = notificationLog.timestamp.toString(),
            onCopy = onCopy
        )

        if (notificationLog.retryCount > 0) {
            DetailRow(
                label = "Retry Count",
                value = notificationLog.retryCount.toString(),
                onCopy = onCopy
            )
        }

        if (notificationLog.lastAttemptAt != null) {
            DetailRow(
                label = "Last Attempt",
                value = formatLastAttemptDate(notificationLog.lastAttemptAt),
                onCopy = onCopy
            )
        }
    }
}

/**
 * HTTP request details section
 */
@Composable
private fun HttpRequestSection(
    httpDetails: HttpRequestResponseDetails,
    onCopy: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailSection(
        title = "HTTP Request",
        icon = Icons.Default.Send,
        modifier = modifier
    ) {
        DetailRow(
            label = "Method",
            value = httpDetails.requestMethod.ifEmpty { "Unknown" },
            onCopy = onCopy
        )

        DetailRow(
            label = "URL",
            value = httpDetails.requestUrl.ifEmpty { "Unknown" },
            onCopy = onCopy,
            isUrl = true
        )

        // Request Headers
        if (httpDetails.requestHeaders.isNotEmpty()) {
            Text(
                text = "Headers:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Limit headers to prevent UI blocking
            httpDetails.requestHeaders.entries.take(10).forEach { entry ->
                DetailRow(
                    label = entry.key,
                    value = entry.value,
                    onCopy = onCopy,
                    isMonospace = true
                )
            }
        }

        // Request Body
        Text(
            text = "Request Body:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )

        CodeBlock(
            code = httpDetails.requestBody?.ifEmpty { "No request body" } ?: "No request body",
            onCopy = { onCopy("Request Body" to (httpDetails.requestBody ?: "")) }
        )
    }
}

/**
 * HTTP response details section
 */
@Composable
private fun HttpResponseSection(
    httpDetails: HttpRequestResponseDetails,
    onCopy: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailSection(
        title = "HTTP Response",
        icon = Icons.Default.Refresh,
        modifier = modifier
    ) {
        val status = httpDetails.responseCode

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Status Code:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            status in 200..299 -> MaterialTheme.colorScheme.primaryContainer
                            status in 400..499 -> MaterialTheme.colorScheme.tertiaryContainer
                            status >= 500 -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        status in 200..299 -> MaterialTheme.colorScheme.onPrimaryContainer
                        status in 400..499 -> MaterialTheme.colorScheme.onTertiaryContainer
                        status >= 500 -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            IconButton(
                onClick = {
                    onCopy("Status Code" to status.toString())
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy status code",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        httpDetails.duration?.let { duration ->
            DetailRow(
                label = "Duration",
                value = "${duration}ms",
                onCopy = onCopy
            )
        }

        // Response Headers
        if (httpDetails.responseHeaders.isNotEmpty()) {
            Text(
                text = "Headers:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Limit headers to prevent UI blocking
            httpDetails.responseHeaders.entries.take(10).forEach { entry ->
                DetailRow(
                    label = entry.key,
                    value = entry.value,
                    onCopy = onCopy,
                    isMonospace = true
                )
            }
        }

        // Response Body
        httpDetails.responseBody?.let { body ->
            Text(
                text = "Response Body:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            CodeBlock(
                code = body.ifEmpty { "No response body" },
                onCopy = { onCopy("Response Body" to body) }
            )
        }
    }
}

/**
 * Error details section for failed notifications
 */
@Composable
private fun ErrorDetailsSection(
    errorMessage: String,
    onCopy: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailSection(
        title = "Error Details",
        icon = Icons.Default.Error,
        modifier = modifier
    ) {
        CodeBlock(
            code = errorMessage.ifEmpty { "No error details available" },
            onCopy = { onCopy("Error Message" to errorMessage) },
            isError = true
        )
    }
}

/**
 * Reusable detail section with header
 */
@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            content()
        }
    }
}

/**
 * Detail row with label, value and copy button
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    onCopy: (Pair<String, String>) -> Unit,
    isUrl: Boolean = false,
    isMonospace: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                color = if (isUrl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = { onCopy(label to value) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Code block with syntax highlighting and copy functionality
 */
@Composable
private fun CodeBlock(
    code: String,
    onCopy: () -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Header with copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code content - limit text size to prevent ANR
            val scrollState = rememberScrollState()
            Text(
                text = code.take(5000), // Limit text to 5000 characters
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = if (isError)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .verticalScroll(scrollState)
            )
        }
    }
}

/**
 * Get priority label from priority value
 */
private fun getPriorityLabel(priority: Int): String {
    return when (priority) {
        in Int.MIN_VALUE..-1 -> "Low ($priority)"
        0 -> "Normal (0)"
        in 1..2 -> "High ($priority)"
        else -> "Urgent ($priority)"
    }
}

/**
 * Format last attempt date efficiently using cached formatter
 */
private fun formatLastAttemptDate(timestamp: Long): String {
    return try {
        val pattern = "MMM dd, yyyy HH:mm:ss"
        val locale = Locale.getDefault()
        val cacheKey = "${pattern}_${locale}"

        val formatter = dateFormatterCache.getOrPut(cacheKey) {
            SimpleDateFormat(pattern, locale)
        }

        formatter.format(Date(timestamp))
    } catch (e: Exception) {
        Timber.w(e, "Error formatting date: $timestamp")
        "Unknown date"
    }
}