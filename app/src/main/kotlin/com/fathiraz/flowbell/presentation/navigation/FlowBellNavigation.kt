package com.fathiraz.flowbell.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fathiraz.flowbell.data.preferences.ThemePreferences
import com.fathiraz.flowbell.presentation.screens.apps.AppListRoute
import com.fathiraz.flowbell.presentation.screens.dashboard.DashboardRoute
import com.fathiraz.flowbell.presentation.screens.notifications.NotificationHistoryRoute
import com.fathiraz.flowbell.presentation.screens.permissions.PermissionRoute
import com.fathiraz.flowbell.presentation.screens.settings.SettingsRoute
import com.fathiraz.flowbell.presentation.screens.webhook.WebhookMainScreen
import com.fathiraz.flowbell.presentation.screens.webhook.WebhookEditScreen

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val navigationItems = listOf(
    NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard),
    NavigationItem("webhook", "Webhook", Icons.Default.Webhook),
    NavigationItem("apps", "Apps", Icons.Default.Apps),
    NavigationItem("history", "History", Icons.Default.History),
    NavigationItem("settings", "Settings", Icons.Default.Settings)
)

@Composable
fun FlowBellNavigation(navController: NavHostController, themePreferences: ThemePreferences) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("permission") {
                PermissionRoute(navController = navController)
            }

            composable("dashboard") {
                DashboardRoute(navController = navController)
            }

            composable("webhook") {
                WebhookMainScreen(navController = navController)
            }

            composable("webhook_edit") {
                WebhookEditScreen(navController = navController)
            }

            composable("apps") {
                AppListRoute(navController = navController)
            }

            composable("history") {
                NotificationHistoryRoute(navController = navController)
            }

            composable("settings") {
                SettingsRoute(navController = navController, themePreferences = themePreferences)
            }
        }
    }
}
