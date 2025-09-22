package com.fathiraz.flowbell.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fathiraz.flowbell.presentation.theme.AppTheme
import com.fathiraz.flowbell.presentation.components.ModernPageHeader
import com.fathiraz.flowbell.presentation.components.ModernPageLayout
import com.fathiraz.flowbell.presentation.components.ModernContentSection
import com.fathiraz.flowbell.presentation.components.ModernLoadingState
import com.fathiraz.flowbell.presentation.components.ModernSpacing
import com.fathiraz.flowbell.presentation.theme.ModernColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Customize your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Appearance Section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Customize the app's visual appearance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Dark Mode Setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Dark Mode",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Toggle between light and dark theme",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = state.isDarkMode,
                onCheckedChange = { onEvent(SettingsEvent.ToggleTheme) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00BCD4),
                    checkedTrackColor = Color(0xFF00BCD4).copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Security Section
        Text(
            text = "Privacy & Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Protect your data and enhance privacy",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Privacy Options Setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Privacy",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Privacy Options",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Enhanced privacy settings for notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = state.isPrivacyOptionsEnabled,
                onCheckedChange = { onEvent(SettingsEvent.TogglePrivacyOptions) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00BCD4),
                    checkedTrackColor = Color(0xFF00BCD4).copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Section
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Manage notification filtering and processing",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Advanced Filters Setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.FilterAlt,
                    contentDescription = "Filters",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Advanced Filters",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Configure advanced notification filtering rules",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = state.isNotificationFiltersEnabled,
                onCheckedChange = { onEvent(SettingsEvent.ToggleNotificationFilters) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00BCD4),
                    checkedTrackColor = Color(0xFF00BCD4).copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Filtering - Coming Soon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FilterAlt,
                contentDescription = "Content Filter",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Content Filtering",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    text = "Filter notifications based on content keywords (Coming soon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Time-based Rules - Coming Soon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "Time Rules",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Time-based Rules",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    text = "Set notification delivery schedules (Coming soon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Route wrapper that integrates with navigation and ViewModel
 */
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

/**
 * Reusable settings section with title and content
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

/**
 * Reusable setting item component for boolean preferences with switch
 */
@Composable
fun ToggleSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics {
                    this.contentDescription = contentDescription
                }
            )
        },
        modifier = modifier
    )
}

/**
 * Placeholder setting item for future features (non-interactive)
 */
@Composable
fun PlaceholderSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        supportingContent = {
            Text(
                text = "$description (Coming soon)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        modifier = modifier.semantics {
            contentDescription = "$title setting. Coming soon."
        }
    )
}

// Preview Composables for design iteration
@Preview(name = "Settings Screen - Light Mode")
@Composable
private fun SettingsScreenLightPreview() {
    AppTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsUiState(
                isDarkMode = false,
                isPrivacyOptionsEnabled = true,
                isNotificationFiltersEnabled = false
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Settings Screen - Dark Mode")
@Composable
private fun SettingsScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        SettingsScreen(
            state = SettingsUiState(
                isDarkMode = true,
                isPrivacyOptionsEnabled = false,
                isNotificationFiltersEnabled = true
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Settings Screen - Loading")
@Composable
private fun SettingsScreenLoadingPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsUiState(
                isLoading = true
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Toggle Setting Item")
@Composable
private fun ToggleSettingItemPreview() {
    AppTheme {
        Card {
            ToggleSettingItem(
                title = "Dark Mode",
                description = "Toggle between light and dark theme",
                icon = Icons.Default.DarkMode,
                isChecked = true,
                onCheckedChange = {},
                contentDescription = "Dark mode toggle"
            )
        }
    }
}

@Preview(name = "Placeholder Setting Item")
@Composable
private fun PlaceholderSettingItemPreview() {
    AppTheme {
        Card {
            PlaceholderSettingItem(
                title = "Advanced Feature",
                description = "This feature will be available soon",
                icon = Icons.Default.Settings
            )
        }
    }
}