package com.fathiraz.flowbell.domain.usecases

import com.fathiraz.flowbell.domain.entities.NotificationStatistics
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * Use case for getting notification statistics.
 * This use case encapsulates the business logic for retrieving notification statistics.
 */
class GetNotificationStatisticsUseCase(
    private val statisticsRepository: NotificationStatisticsRepository
) {
    
    /**
     * Get current notification statistics.
     */
    suspend operator fun invoke(): Flow<NotificationStatistics> {
        Timber.d("GetNotificationStatisticsUseCase: Getting notification statistics")
        return statisticsRepository.getNotificationStatistics()
    }
    
    /**
     * Get statistics for a specific date range.
     */
    suspend fun getStatisticsInRange(
        startDate: Long,
        endDate: Long
    ): Flow<NotificationStatistics> {
        Timber.d("GetNotificationStatisticsUseCase: Getting statistics in range $startDate to $endDate")
        return statisticsRepository.getStatisticsInRange(startDate, endDate)
    }
}