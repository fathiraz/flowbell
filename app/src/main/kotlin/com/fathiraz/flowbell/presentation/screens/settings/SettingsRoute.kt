package com.fathiraz.flowbell.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fathiraz.flowbell.data.preferences.ThemePreferences
import kotlinx.coroutines.launch

data class SettingItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isToggle: Boolean = false,
    val isEnabled: Boolean = false,
    val isComingSoon: Boolean = false
)

/**
 * Settings route composable
 */
@Composable
fun SettingsRoute(
    navController: NavController,
    themePreferences: ThemePreferences,
    modifier: Modifier = Modifier
) {
    val darkMode by themePreferences.isDarkMode.collectAsState(initial = false)
    var advancedFilters by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Appearance section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Customize the app's visual appearance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingRow(
            title = "Dark Mode",
            description = "Toggle between light and dark theme",
            icon = Icons.Default.DarkMode,
            isToggle = true,
            isEnabled = darkMode,
            onToggle = { enabled ->
                coroutineScope.launch {
                    themePreferences.setDarkMode(enabled)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Notifications section
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage notification filtering and processing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingRow(
            title = "Advanced Filters",
            description = "Configure advanced notification filtering rules",
            icon = Icons.Default.FilterAlt,
            isToggle = true,
            isEnabled = advancedFilters,
            onToggle = { advancedFilters = it }
        )

        SettingRow(
            title = "Content Filtering",
            description = "Filter notifications based on content keywords",
            icon = Icons.Default.FilterAlt,
            isComingSoon = true
        )

        SettingRow(
            title = "Time-based Rules",
            description = "Set notification delivery schedules",
            icon = Icons.Default.Schedule,
            isComingSoon = true
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    icon: ImageVector,
    isToggle: Boolean = false,
    isEnabled: Boolean = false,
    isComingSoon: Boolean = false,
    onToggle: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isComingSoon) {
                        Text(
                            text = "(Coming soon)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (isToggle && !isComingSoon) {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}