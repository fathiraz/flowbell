package com.fathiraz.flowbell.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.presentation.components.MediumPackageIcon
import com.fathiraz.flowbell.presentation.components.SkeletonLoader
import com.fathiraz.flowbell.presentation.components.AnimatedListItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notification history route composable with ViewModel integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotificationHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    // Infinite scroll logic
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 5 // Load more when 5 items from end
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !uiState.isLoadingMore && uiState.hasMoreItems) {
                viewModel.onEvent(HistoryEvent.LoadMore)
            }
        }
    }
    var selectedNotification by remember { mutableStateOf<NotificationLog?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onEvent(HistoryEvent.Refresh) },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isLoading) "Loading..." else "${uiState.filteredLogs.size} notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Refresh button
            IconButton(onClick = { viewModel.onEvent(HistoryEvent.Refresh) }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { query ->
                viewModel.onEvent(HistoryEvent.SearchQueryChanged(query))
            },
            placeholder = { Text("Search by app name or title...") },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onEvent(HistoryEvent.ClearSearch) }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                NotificationFilter.All to "All",
                NotificationFilter.Sent to "Sent", 
                NotificationFilter.Failed to "Failed"
            )
            
            filters.forEach { (filter, label) ->
                val isSelected = uiState.selectedFilter == filter
                FilterChip(
                    onClick = { 
                        viewModel.onEvent(HistoryEvent.FilterSelected(filter))
                    },
                    label = { 
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    selected = isSelected,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Loading, empty state, or notifications list
        when {
            uiState.isLoading -> {
                // Loading state with skeleton
                HistorySkeleton()
            }

            uiState.filteredLogs.isEmpty() -> {
                // Empty state with contextual messages
                val (title, subtitle) = when {
                    uiState.searchQuery.isNotEmpty() -> {
                        "No notifications found" to "Try adjusting your search terms"
                    }
                    uiState.selectedFilter != null && uiState.selectedFilter != NotificationFilter.All -> {
                        val filterName = when (uiState.selectedFilter) {
                            NotificationFilter.Sent -> "sent"
                            NotificationFilter.Failed -> "failed"
                            else -> "filtered"
                        }
                        "No $filterName notifications" to "Try selecting a different filter"
                    }
                    else -> {
                        "No notifications yet" to "Notification history will appear here once you start receiving notifications"
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "No history",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                // Notifications list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(uiState.filteredLogs) { notification ->
                        NotificationHistoryCard(
                            notification = notification,
                            onClick = {
                                selectedNotification = notification
                                showBottomSheet = true
                            }
                        )
                    }

                    // Loading indicator for infinite scroll
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Notification detail bottom sheet
    if (showBottomSheet && selectedNotification != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            NotificationDetailBottomSheet(
                notification = selectedNotification!!,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
    }}

@Composable
private fun NotificationHistoryCard(
    notification: NotificationLog,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val formattedTime = remember(notification.timestamp) {
        dateFormat.format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large app icon (left)
            MediumPackageIcon(
                packageName = notification.packageName,
                appName = notification.appName,
                modifier = Modifier.size(56.dp)
            )

            // Content column (middle, weight = 1f)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // App name (bold, titleMedium)
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Notification title (bodyLarge, 1 line, ellipsis)
                Text(
                    text = notification.notificationTitle.takeIf { it.isNotEmpty() } ?: "Notification",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Notification text (bodyMedium, gray, 1 line, ellipsis)
                Text(
                    text = notification.notificationText.takeIf { it.isNotEmpty() } ?: "No content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Timestamp at bottom (caption, lighter gray)
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Status badge (right, vertical alignment center)
            val (statusColor, statusBackgroundColor) = when (notification.status.name.lowercase()) {
                "sent" -> Color(0xFF4CAF50) to Color(0xFF4CAF50).copy(alpha = 0.1f)
                "failed" -> Color(0xFFF44336) to Color(0xFFF44336).copy(alpha = 0.1f)
                "pending" -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFF757575) to Color(0xFF757575).copy(alpha = 0.1f)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = statusBackgroundColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, RoundedCornerShape(4.dp))
                    )
                    
                    // Status text
                    Text(
                        text = notification.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailBottomSheet(
    notification: NotificationLog,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(notification.timestamp) {
        dateFormat.format(Date(notification.timestamp))
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notification Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider()

        // Notification details section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailRow("Title", notification.notificationTitle.takeIf { it.isNotEmpty() } ?: "No title")
                DetailRow("Content", notification.notificationText.takeIf { it.isNotEmpty() } ?: "No content")
                DetailRow("App", "${notification.appName} (${notification.packageName})")
                DetailRow("Time", formattedTime)
                DetailRow("Status", notification.status.name)

                if (notification.errorMessage != null) {
                    HorizontalDivider()
                    DetailRow("Error", notification.errorMessage!!)
                }
            }
        }

        // HTTP details section
        Text(
            text = "HTTP Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                notification.httpDetails?.let { httpDetails ->
                    DetailRow("URL", httpDetails.requestUrl)
                    DetailRow("Method", httpDetails.requestMethod)
                    DetailRow("Response Code", httpDetails.responseCode.toString())
                    DetailRow("Response Time", "${httpDetails.duration}ms")

                    if (httpDetails.responseBody?.isNotEmpty() == true) {
                        HorizontalDivider()
                        DetailRow("Response", httpDetails.responseBody!!.take(200) + if (httpDetails.responseBody!!.length > 200) "..." else "")
                    }
                } ?: run {
                    Text(
                        text = "No HTTP details available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Close")
        }

        // Extra padding for bottom sheet
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HistorySkeleton() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Notification list skeletons only
        repeat(8) { index ->
            AnimatedListItem(
                visible = true,
                animationDelay = index * 50  // Faster animation: 0ms, 50ms, 100ms, etc. (all appear within 400ms)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Large app icon skeleton (56dp)
                        SkeletonLoader(width = 56.dp, height = 56.dp, cornerRadius = 12.dp)

                        // Content column skeleton
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // App name skeleton
                            SkeletonLoader(width = 120.dp, height = 20.dp)
                            
                            // Notification title skeleton
                            SkeletonLoader(width = 180.dp, height = 18.dp)
                            
                            // Notification text skeleton
                            SkeletonLoader(width = 160.dp, height = 16.dp)
                            
                            // Timestamp skeleton
                            SkeletonLoader(width = 80.dp, height = 14.dp)
                        }

                        // Status badge skeleton (right)
                        SkeletonLoader(width = 70.dp, height = 32.dp, cornerRadius = 12.dp)
                    }
                }
            }
        }
    }
}