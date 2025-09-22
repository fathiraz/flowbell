package com.fathiraz.flowbell.presentation.events

sealed class AnalyticsEvent {
    object Refresh : AnalyticsEvent()
    data class UpdateDateRange(val days: Int) : AnalyticsEvent()
}