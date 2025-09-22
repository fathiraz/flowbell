package com.fathiraz.flowbell.data.local.database.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import com.fathiraz.flowbell.data.local.database.dao.AppPreferencesDao
import com.fathiraz.flowbell.data.local.database.entities.AppPreferences
import com.fathiraz.flowbell.data.AppInfo

class AppPreferencesRepository(
    private val appPreferencesDao: AppPreferencesDao
) {
    
    fun getAllPreferences(): Flow<List<AppPreferences>> {
        return appPreferencesDao.getAllPreferences()
    }
    
    fun getPreference(packageName: String): Flow<AppPreferences?> {
        return appPreferencesDao.getPreferenceFlow(packageName)
    }
    
    fun getEnabledApps(): Flow<List<AppPreferences>> {
        return appPreferencesDao.getEnabledApps()
    }
    
    suspend fun setForwardingEnabled(packageName: String, enabled: Boolean) {
        try {
            Timber.d("🔧 Setting forwarding for $packageName to $enabled")

            // Use upsert pattern: try to update first, if no rows affected then insert
            val existingPreference = appPreferencesDao.getPreference(packageName)

            if (existingPreference != null) {
                // Update existing preference
                Timber.d("📝 Found existing preference for $packageName: enabled=${existingPreference.isForwardingEnabled}")
                appPreferencesDao.updateForwardingStatus(packageName, enabled, System.currentTimeMillis())
                Timber.d("✅ Updated existing preference for $packageName to $enabled")
            } else {
                // Insert new preference
                Timber.d("➕ No existing preference found for $packageName, creating new one")
                val newPreference = AppPreferences(
                    packageName = packageName,
                    isForwardingEnabled = enabled
                )
                appPreferencesDao.insertPreference(newPreference)
                Timber.d("✅ Created new preference for $packageName with enabled=$enabled")
            }

            // Verify the change was saved
            val verifyPreference = appPreferencesDao.getPreference(packageName)
            Timber.d("🔍 Verification: $packageName now has enabled=${verifyPreference?.isForwardingEnabled}")

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set forwarding status for $packageName")
            throw e
        }
    }
    
    suspend fun getForwardingStatus(packageName: String): Boolean {
        return try {
            val preference = appPreferencesDao.getPreference(packageName)
            preference?.isForwardingEnabled ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to get forwarding status for $packageName")
            false
        }
    }
    
    suspend fun initializePreferencesForApps(apps: List<AppInfo>) {
        try {
            // Get existing preferences synchronously
            val existingPreferences = appPreferencesDao.getAllPreferencesSync()
            val existingPackageNames = existingPreferences.map { it.packageName }.toSet()
            
            val newPreferences = apps
                .filter { it.packageName !in existingPackageNames }
                .map { appInfo ->
                    AppPreferences(
                        packageName = appInfo.packageName,
                        isForwardingEnabled = false // Default to disabled
                    )
                }
            
            if (newPreferences.isNotEmpty()) {
                appPreferencesDao.insertPreferences(newPreferences)
                Timber.d("Initialized preferences for ${newPreferences.size} new apps")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize preferences for apps")
            throw e
        }
    }
    
    suspend fun deletePreference(packageName: String) {
        try {
            appPreferencesDao.deletePreferenceByPackage(packageName)
            Timber.d("Deleted preference for $packageName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete preference for $packageName")
            throw e
        }
    }
}
