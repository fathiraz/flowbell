package com.fathiraz.flowbell.presentation.state

data class AppStatistic(
    val appName: String = "",
    val packageName: String = "",
    val notificationCount: Int = 0,
    val successRate: Float = 0f
)