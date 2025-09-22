package com.fathiraz.flowbell.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.fathiraz.flowbell.data.preferences.ThemePreferences
import com.fathiraz.flowbell.presentation.navigation.FlowBellNavigation

@Composable
fun FlowBellApp(themePreferences: ThemePreferences) {
    val navController = rememberNavController()
    FlowBellNavigation(navController = navController, themePreferences = themePreferences)
}