package com.fathiraz.flowbell.presentation.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fathiraz.flowbell.presentation.components.ModernPageHeader
import com.fathiraz.flowbell.presentation.components.ModernPageLayout
import com.fathiraz.flowbell.presentation.components.ModernContentSection
import com.fathiraz.flowbell.presentation.components.ModernSpacing
import com.fathiraz.flowbell.presentation.theme.ModernColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState,
    onEvent: (AnalyticsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernPageLayout(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Header with refresh action
        ModernPageHeader(
            title = "Analytics",
            subtitle = "Notification insights and statistics",
            icon = Icons.Default.Analytics,
            actions = {
                IconButton(
                    onClick = { onEvent(AnalyticsEvent.Refresh) },
                    enabled = !state.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (state.isLoading) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                              else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ModernColors.TealPrimary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.large),
                contentPadding = PaddingValues(bottom = ModernSpacing.extraLarge)
            ) {
                // Overview Cards
                item {
                    OverviewSection(state = state)
                }

                // Date Range Selector
                item {
                    DateRangeSelector(
                        selectedDays = state.selectedDays,
                        onSelectionChange = { onEvent(AnalyticsEvent.UpdateDateRange(it)) }
                    )
                }

                // Success Rate Card
                item {
                    SuccessRateCard(state = state)
                }

                // Top Apps Section
                if (state.topApps.isNotEmpty()) {
                    item {
                        TopAppsSection(topApps = state.topApps)
                    }
                }

                // Daily Trends Section
                if (state.dailyStats.isNotEmpty()) {
                    item {
                        DailyTrendsSection(dailyStats = state.dailyStats)
                    }
                }

                // Hourly Distribution
                if (state.hourlyDistribution.isNotEmpty()) {
                    item {
                        HourlyDistributionSection(hourlyStats = state.hourlyDistribution)
                    }
                }

                // Status Breakdown
                if (state.notificationsByStatus.isNotEmpty()) {
                    item {
                        StatusBreakdownSection(statusStats = state.notificationsByStatus)
                    }
                }

                // Last Updated Info
                if (state.lastUpdated.isNotEmpty()) {
                    item {
                        LastUpdatedInfo(lastUpdated = state.lastUpdated)
                    }
                }
            }
        }

        // Error handling
        state.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Handle error display
            }
        }
    }
}

@Composable
private fun OverviewSection(state: AnalyticsUiState) {
    ModernContentSection(
        title = "Overview",
        subtitle = "Key metrics at a glance"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.medium)
        ) {
            MetricCard(
                title = "Total",
                value = state.totalNotifications.toString(),
                icon = Icons.Default.Assessment,
                color = ModernColors.Blue,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Success",
                value = state.successfulWebhooks.toString(),
                icon = Icons.Default.CheckCircle,
                color = ModernColors.Green,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Failed",
                value = state.failedWebhooks.toString(),
                icon = Icons.Default.Error,
                color = ModernColors.Red,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateRangeSelector(
    selectedDays: Int,
    onSelectionChange: (Int) -> Unit
) {
    ModernContentSection(
        title = "Time Range",
        subtitle = "Select period for analysis"
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            val options = listOf(
                7 to "7 Days",
                14 to "14 Days",
                30 to "30 Days",
                90 to "90 Days"
            )

            items(options) { (days, label) ->
                FilterChip(
                    onClick = { onSelectionChange(days) },
                    label = { Text(label) },
                    selected = selectedDays == days,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ModernColors.TealPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun SuccessRateCard(state: AnalyticsUiState) {
    ModernContentSection(
        title = "Success Rate",
        subtitle = "Webhook delivery performance"
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when {
                        state.successRate >= 95 -> ModernColors.Green.copy(alpha = 0.1f)
                        state.successRate >= 80 -> ModernColors.Orange.copy(alpha = 0.1f)
                        else -> ModernColors.Red.copy(alpha = 0.1f)
                    }
                )
                .padding(ModernSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.small)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = state.successRate / 100f,
                    label = "success_rate_animation"
                )

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(80.dp),
                    color = when {
                        state.successRate >= 95 -> ModernColors.Green
                        state.successRate >= 80 -> ModernColors.Orange
                        else -> ModernColors.Red
                    },
                    strokeWidth = 8.dp,
                )

                Text(
                    text = "${state.successRate}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Success Rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopAppsSection(topApps: List<AppStatistic>) {
    ModernContentSection(
        title = "Top Apps",
        subtitle = "Most active notification sources"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            topApps.take(5).forEach { app ->
                AppStatisticRow(app = app)
            }
        }
    }
}

@Composable
private fun AppStatisticRow(app: AppStatistic) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(ModernSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ModernSpacing.medium)
    ) {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = ModernColors.TealPrimary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${app.notificationCount} notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${app.successRate}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (app.successRate >= 90) ModernColors.Green else ModernColors.Orange
            )
            Text(
                text = "success",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyTrendsSection(dailyStats: List<DailyStatistic>) {
    ModernContentSection(
        title = "Daily Trends",
        subtitle = "Notification activity over time"
    ) {
        // Simple bar chart representation
        Column(
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            dailyStats.takeLast(7).forEach { day ->
                DailyStatRow(day = day, maxValue = dailyStats.maxOfOrNull { it.notificationCount } ?: 1)
            }
        }
    }
}

@Composable
private fun DailyStatRow(day: DailyStatistic, maxValue: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ModernSpacing.medium)
    ) {
        Text(
            text = day.date,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(48.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            val progress = if (maxValue > 0) day.notificationCount.toFloat() / maxValue else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        ModernColors.TealPrimary.copy(alpha = 0.8f),
                        RoundedCornerShape(12.dp)
                    )
            )
        }

        Text(
            text = day.notificationCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HourlyDistributionSection(hourlyStats: List<HourlyStatistic>) {
    ModernContentSection(
        title = "Hourly Distribution",
        subtitle = "Notification patterns throughout the day"
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.extraSmall)
        ) {
            items(hourlyStats) { hourStat ->
                HourlyBar(
                    hour = hourStat.hour,
                    count = hourStat.notificationCount,
                    maxCount = hourlyStats.maxOfOrNull { it.notificationCount } ?: 1
                )
            }
        }
    }
}

@Composable
private fun HourlyBar(hour: Int, count: Long, maxCount: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ModernSpacing.extraSmall)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val heightFraction = if (maxCount > 0) count.toFloat() / maxCount else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction)
                    .background(
                        ModernColors.TealPrimary.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
            )
        }

        Text(
            text = String.format("%02d", hour),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBreakdownSection(statusStats: List<StatusStatistic>) {
    ModernContentSection(
        title = "Status Breakdown",
        subtitle = "Notification processing results"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            statusStats.forEach { status ->
                StatusRow(status = status)
            }
        }
    }
}

@Composable
private fun StatusRow(status: StatusStatistic) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ModernSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ModernSpacing.medium)
    ) {
        val statusColor = when (status.status) {
            "SENT" -> ModernColors.Green
            "FAILED" -> ModernColors.Red
            "PENDING" -> ModernColors.Orange
            "PROCESSING" -> ModernColors.Blue
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Box(
            modifier = Modifier
                .size(12.dp)
                .background(statusColor, RoundedCornerShape(6.dp))
        )

        Text(
            text = status.status.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${status.count}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "(${status.percentage}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LastUpdatedInfo(lastUpdated: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ModernSpacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Last updated: ${lastUpdated.substring(0, 16)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(ModernSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.small)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}