package com.fathiraz.flowbell.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fathiraz.flowbell.domain.entities.NotificationLog
import com.fathiraz.flowbell.domain.entities.NotificationQueueStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.map

@Composable
fun NotificationHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with real-time count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${uiState.filteredLogs.size} notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Manual refresh button
                IconButton(
                    onClick = { viewModel.onEvent(HistoryEvent.Refresh) }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF00BCD4)
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = uiState.searchApp,
                onValueChange = { viewModel.onEvent(HistoryEvent.SearchByApp(it)) },
                placeholder = { Text("Search by app name...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF00BCD4)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BCD4),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00BCD4))
                }
            }

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Notifications list
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.filteredLogs,
                    key = { it.id }
                ) { notification ->
                    NotificationHistoryItem(
                        notification = notification,
                        onItemClick = {
                            viewModel.onEvent(HistoryEvent.ShowNotificationDetails(notification))
                        }
                    )
                }

                // Load more indicator
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
                                color = Color(0xFF00BCD4)
                            )
                        }
                    }
                }

                // Load more trigger
                if (uiState.hasMoreItems && !uiState.isLoadingMore && uiState.filteredLogs.isNotEmpty()) {
                    item {
                        LaunchedEffect(Unit) {
                            viewModel.onEvent(HistoryEvent.LoadMore)
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun NotificationHistoryItem(
    notification: NotificationLog,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App icon placeholder with first letter of app name
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(getStatusColor(notification.status), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.appName.firstOrNull()?.uppercaseChar()?.toString() ?: "N",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = notification.notificationTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTimestamp(notification.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (notification.httpDetails != null) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "HTTP details available",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00BCD4)
                        )
                    }
                }
            }

            // Status and action
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Status badge
                Surface(
                    color = getStatusColor(notification.status),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = notification.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap for details",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00BCD4)
                )
            }
        }
    }
}

@Composable
private fun getStatusColor(status: NotificationQueueStatus): Color {
    return when (status) {
        NotificationQueueStatus.SENT -> Color(0xFF4CAF50)
        NotificationQueueStatus.FAILED -> Color(0xFFF44336)
        NotificationQueueStatus.PENDING -> Color(0xFFFF9800)
        NotificationQueueStatus.PROCESSING -> Color(0xFF2196F3)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

