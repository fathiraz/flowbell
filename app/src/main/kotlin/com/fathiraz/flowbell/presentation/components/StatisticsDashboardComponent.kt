package com.fathiraz.flowbell.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fathiraz.flowbell.domain.entities.*
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main statistics dashboard component
 */
@Composable
fun StatisticsDashboard(
    statistics: NotificationStatistics,
    selectedPeriod: StatisticsPeriod,
    onPeriodChange: (StatisticsPeriod) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with period selector and refresh
        StatisticsDashboardHeader(
            selectedPeriod = selectedPeriod,
            onPeriodChange = onPeriodChange,
            onRefresh = onRefresh
        )

        // Queue health indicator
        QueueHealthIndicator(
            queueHealth = statistics.queueHealth
        )

        // Key metrics cards
        StatisticsMetricCards(
            statistics = statistics
        )

        // Success rate trend
        if (statistics.weeklyStats.dailyTotals.sum() > 0) {
            SuccessRateTrend(
                weeklyStats = statistics.weeklyStats
            )
        }

        // Top failing apps (if any)
        if (statistics.topFailingApps.isNotEmpty()) {
            TopFailingApps(
                topFailingApps = statistics.topFailingApps
            )
        }

        // Recent activity
        if (statistics.recentActivity.isNotEmpty()) {
            RecentActivity(
                recentActivity = statistics.recentActivity.take(5)
            )
        }
    }
}

/**
 * Header with period selector and refresh button
 */
@Composable
private fun StatisticsDashboardHeader(
    selectedPeriod: StatisticsPeriod,
    onPeriodChange: (StatisticsPeriod) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Statistics Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Period selector
            var expanded by remember { mutableStateOf(false) }

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.menuAnchor()
                ) {
                    Text(selectedPeriod.displayName)
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    StatisticsPeriod.values().forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period.displayName) },
                            onClick = {
                                onPeriodChange(period)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Refresh button
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh statistics"
                )
            }
        }
    }
}

/**
 * Queue health indicator with warnings
 */
@Composable
private fun QueueHealthIndicator(
    queueHealth: QueueHealth,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (queueHealth.healthColor) {
                HealthColor.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
                HealthColor.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                HealthColor.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (queueHealth.healthColor) {
                    HealthColor.HEALTHY -> Icons.Default.CheckCircle
                    HealthColor.WARNING -> Icons.Default.Warning
                    HealthColor.CRITICAL -> Icons.Default.Error
                },
                contentDescription = "Health status",
                tint = when (queueHealth.healthColor) {
                    HealthColor.HEALTHY -> MaterialTheme.colorScheme.primary
                    HealthColor.WARNING -> MaterialTheme.colorScheme.tertiary
                    HealthColor.CRITICAL -> MaterialTheme.colorScheme.error
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (queueHealth.isHealthy) "System Healthy" else "Issues Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (queueHealth.warnings.isNotEmpty()) {
                    queueHealth.warnings.take(2).forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (queueHealth.isHealthy) {
                    Text(
                        text = "Queue: ${queueHealth.queueSize} notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Key metrics cards
 */
@Composable
private fun StatisticsMetricCards(
    statistics: NotificationStatistics,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            MetricCard(
                title = "Total",
                value = "${statistics.totalNotifications}",
                icon = Icons.Default.HourglassEmpty,
                iconColor = MaterialTheme.colorScheme.primary
            )
        }

        item {
            MetricCard(
                title = "Success",
                value = "${statistics.successful}",
                subtitle = "${statistics.successRate.toInt()}%",
                icon = Icons.Default.CheckCircle,
                iconColor = MaterialTheme.colorScheme.tertiary
            )
        }

        item {
            MetricCard(
                title = "Failed",
                value = "${statistics.failed}",
                subtitle = "${statistics.failureRate.toInt()}%",
                icon = Icons.Default.Error,
                iconColor = MaterialTheme.colorScheme.error
            )
        }

        item {
            MetricCard(
                title = "Pending",
                value = "${statistics.pending + statistics.processing}",
                subtitle = "In queue",
                icon = Icons.Default.HourglassEmpty,
                iconColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Individual metric card
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Success rate trend visualization
 */
@Composable
private fun SuccessRateTrend(
    weeklyStats: WeeklyStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "7-Day Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = when (weeklyStats.trend) {
                        StatisticsTrend.IMPROVING -> Icons.Default.TrendingUp
                        StatisticsTrend.STABLE -> Icons.Default.TrendingFlat
                        StatisticsTrend.DECLINING -> Icons.Default.TrendingDown
                        StatisticsTrend.UP -> Icons.Default.TrendingUp
                        StatisticsTrend.DOWN -> Icons.Default.TrendingDown
                    },
                    contentDescription = "Trend",
                    tint = when (weeklyStats.trend) {
                        StatisticsTrend.IMPROVING -> MaterialTheme.colorScheme.tertiary
                        StatisticsTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        StatisticsTrend.DECLINING -> MaterialTheme.colorScheme.error
                        StatisticsTrend.UP -> MaterialTheme.colorScheme.tertiary
                        StatisticsTrend.DOWN -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = weeklyStats.trend.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (weeklyStats.trend) {
                        StatisticsTrend.IMPROVING -> MaterialTheme.colorScheme.tertiary
                        StatisticsTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        StatisticsTrend.DECLINING -> MaterialTheme.colorScheme.error
                        StatisticsTrend.UP -> MaterialTheme.colorScheme.tertiary
                        StatisticsTrend.DOWN -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simple bar chart representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyStats.dailyTotals.forEachIndexed { index, total ->
                    val success = weeklyStats.dailySuccess[index]
                    val height = if (weeklyStats.dailyTotals.maxOrNull() ?: 0 > 0) {
                        ((total.toFloat() / weeklyStats.dailyTotals.maxOrNull()!!) * 40).dp
                    } else 2.dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(height)
                                .background(
                                    if (total > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = getDayLabel(index),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top failing apps section
 */
@Composable
private fun TopFailingApps(
    topFailingApps: List<AppFailureStatistic>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Apps with Most Failures",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            topFailingApps.take(3).forEach { appStat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appStat.appName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${appStat.failureCount}/${appStat.totalCount} failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${appStat.failureRate.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Recent activity section
 */
@Composable
private fun RecentActivity(
    recentActivity: List<RecentActivity>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            recentActivity.forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (activity.status) {
                                    NotificationQueueStatus.SENT -> MaterialTheme.colorScheme.tertiary
                                    NotificationQueueStatus.FAILED -> MaterialTheme.colorScheme.error
                                    NotificationQueueStatus.PENDING -> MaterialTheme.colorScheme.secondary
                                    NotificationQueueStatus.PROCESSING -> MaterialTheme.colorScheme.primary
                                },
                                CircleShape
                            )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activity.appName}: ${activity.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                        Text(
                            text = formatTimestamp(activity.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = activity.status.name.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (activity.status) {
                            NotificationQueueStatus.SENT -> MaterialTheme.colorScheme.tertiary
                            NotificationQueueStatus.FAILED -> MaterialTheme.colorScheme.error
                            NotificationQueueStatus.PENDING -> MaterialTheme.colorScheme.secondary
                            NotificationQueueStatus.PROCESSING -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get day label
 */
private fun getDayLabel(index: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -(6 - index))
    return when (index) {
        6 -> "Today"
        5 -> "Yest"
        else -> SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).take(3)
    }
}

/**
 * Helper function to format timestamp
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}