package com.fathiraz.flowbell.domain.usecases

import com.fathiraz.flowbell.domain.entities.App
import com.fathiraz.flowbell.domain.repositories.AppRepository
import timber.log.Timber

/**
 * Use case for managing app preferences.
 * This use case encapsulates the business logic for app forwarding preferences.
 */
class ManageAppPreferencesUseCase(
    private val appRepository: AppRepository
) {
    
    /**
     * Get all installed apps.
     */
    suspend fun getAllApps(): kotlinx.coroutines.flow.Flow<List<App>> {
        Timber.d("ManageAppPreferencesUseCase: Getting all apps")
        return appRepository.getAllApps()
    }
    
    /**
     * Get apps with forwarding enabled.
     */
    suspend fun getAppsWithForwardingEnabled(): kotlinx.coroutines.flow.Flow<List<App>> {
        Timber.d("ManageAppPreferencesUseCase: Getting apps with forwarding enabled")
        return appRepository.getAppsWithForwardingEnabled()
    }
    
    /**
     * Toggle app forwarding status.
     */
    suspend fun toggleAppForwarding(packageName: String, isEnabled: Boolean): Result<Unit> {
        Timber.d("ManageAppPreferencesUseCase: Toggling forwarding for $packageName to $isEnabled")
        
        return try {
            val result = appRepository.updateAppForwardingStatus(packageName, isEnabled)
            if (result.isSuccess) {
                Timber.d("ManageAppPreferencesUseCase: Successfully updated forwarding status for $packageName")
            } else {
                Timber.e("ManageAppPreferencesUseCase: Failed to update forwarding status for $packageName")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageAppPreferencesUseCase: Exception updating forwarding status")
            Result.failure(e)
        }
    }
    
    /**
     * Refresh installed apps list.
     */
    suspend fun refreshInstalledApps(): Result<Unit> {
        Timber.d("ManageAppPreferencesUseCase: Refreshing installed apps")
        
        return try {
            val result = appRepository.refreshInstalledApps()
            if (result.isSuccess) {
                Timber.d("ManageAppPreferencesUseCase: Successfully refreshed installed apps")
            } else {
                Timber.e("ManageAppPreferencesUseCase: Failed to refresh installed apps")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "ManageAppPreferencesUseCase: Exception refreshing installed apps")
            Result.failure(e)
        }
    }
}