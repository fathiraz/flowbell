package com.fathiraz.flowbell.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fathiraz.flowbell.domain.entities.NotificationStatistics

@Composable
fun SimpleStatisticsComponent(
    statistics: NotificationStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Statistics",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total: ${statistics.totalNotifications}")
            Text("Successful: ${statistics.successful}")
            Text("Failed: ${statistics.failed}")
            Text("Success Rate: ${String.format("%.1f", statistics.successRate * 100)}%")
        }
    }
}