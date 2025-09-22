package com.fathiraz.flowbell.domain.entities

/**
 * Enum representing health color for UI
 */
enum class HealthColor {
    HEALTHY,
    WARNING,
    CRITICAL
}

/**
 * Data class representing the health status of the notification queue
 */
data class QueueHealth(
    val healthColor: HealthColor = HealthColor.HEALTHY,
    val isHealthy: Boolean = true,
    val warnings: List<String> = emptyList(),
    val queueSize: Int = 0
) {
    companion object {
        val HEALTHY = QueueHealth(HealthColor.HEALTHY, true)
        val WARNING = QueueHealth(HealthColor.WARNING, false)
        val CRITICAL = QueueHealth(HealthColor.CRITICAL, false)
    }
}
