package com.fathiraz.flowbell.presentation.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

/**
 * Permission route composable
 */
@Composable
fun PermissionRoute(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Implement permission screen
}

/**
 * Permission screen for requesting notification access
 */
@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCheckingPermission by remember { mutableStateOf(false) }

    // Continuously check permission status
    LaunchedEffect(Unit) {
        while (true) {
            val hasPermission = com.fathiraz.flowbell.core.utils.PermissionUtils.isNotificationListenerPermissionGranted(context)
            if (hasPermission) {
                onPermissionGranted()
                break
            }
            delay(1000) // Check every second
        }
    }

    // Handle settings button click
    LaunchedEffect(isCheckingPermission) {
        if (isCheckingPermission) {
            delay(2000) // Give time for settings to open and user to make changes
            isCheckingPermission = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color(0xFF00BCD4).copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notification Permission",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF00BCD4)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Enable Notification Access",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "FlowBell needs permission to read notifications from other apps to forward them to your webhook.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How to enable:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PermissionStep(
                    number = "1",
                    text = "Tap 'Open Settings' below"
                )

                PermissionStep(
                    number = "2", 
                    text = "Find 'FlowBell' in the list"
                )

                PermissionStep(
                    number = "3",
                    text = "Toggle the switch to enable FlowBell"
                )

                PermissionStep(
                    number = "4",
                    text = "Return to this app (it will close automatically)"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Open Settings Button
        Button(
            onClick = {
                isCheckingPermission = true
                com.fathiraz.flowbell.core.utils.PermissionUtils.openNotificationListenerSettings(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            ),
            enabled = !isCheckingPermission
        ) {
            if (isCheckingPermission) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Opening Settings...")
            } else {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Open Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    }
}

@Composable
private fun PermissionStep(
    number: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    Color(0xFF00BCD4),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
