package com.fathiraz.flowbell.presentation.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * Dashboard route composable
 */
@Composable
fun DashboardRoute(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: DashboardViewModel = koinViewModel()

    DashboardScreen(
        viewModel = viewModel,
        navController = navController,
        modifier = modifier
    )
}