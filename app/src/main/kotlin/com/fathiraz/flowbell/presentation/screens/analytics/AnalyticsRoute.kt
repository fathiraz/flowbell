package com.fathiraz.flowbell.presentation.screens.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalyticsRoute(
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AnalyticsScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}