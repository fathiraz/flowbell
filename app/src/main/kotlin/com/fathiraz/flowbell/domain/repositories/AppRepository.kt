package com.fathiraz.flowbell.domain.repositories

import com.fathiraz.flowbell.domain.entities.App
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app-related operations.
 * This interface defines the contract for app data operations.
 */
interface AppRepository {
    
    /**
     * Get all installed apps as a Flow.
     */
    suspend fun getAllApps(): Flow<List<App>>
    
    /**
     * Get apps with forwarding enabled.
     */
    suspend fun getAppsWithForwardingEnabled(): Flow<List<App>>
    
    /**
     * Update app forwarding status.
     */
    suspend fun updateAppForwardingStatus(packageName: String, isEnabled: Boolean): Result<Unit>
    
    /**
     * Get app by package name.
     */
    suspend fun getAppByPackageName(packageName: String): Result<App>
    
    /**
     * Refresh installed apps list.
     */
    suspend fun refreshInstalledApps(): Result<Unit>
}
