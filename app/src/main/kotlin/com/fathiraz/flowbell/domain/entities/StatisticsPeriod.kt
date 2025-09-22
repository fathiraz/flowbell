package com.fathiraz.flowbell.domain.entities

/**
 * Enum representing different time periods for statistics
 */
enum class StatisticsPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
