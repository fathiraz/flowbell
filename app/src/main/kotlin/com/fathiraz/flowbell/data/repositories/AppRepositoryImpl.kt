package com.fathiraz.flowbell.data.repositories

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.fathiraz.flowbell.data.local.database.repository.AppPreferencesRepository
import com.fathiraz.flowbell.domain.entities.App
import com.fathiraz.flowbell.data.AppInfo
import com.fathiraz.flowbell.domain.repositories.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class AppRepositoryImpl(
    private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository
) : AppRepository {

    /**
     * Check if an app has notification access/permission
     */
    private fun hasNotificationAccess(packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager

            // For Android 13+ (API 33+), check POST_NOTIFICATIONS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = packageManager.checkPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    packageName
                )
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    return true
                }
            }

            // For older versions or as fallback, check if app can post notifications
            // This requires creating a context for the target app
            val targetContext = context.createPackageContext(packageName, 0)
            val notificationManager = NotificationManagerCompat.from(targetContext)
            notificationManager.areNotificationsEnabled()

        } catch (e: Exception) {
            // If we can't determine notification access, assume app has access
            // This is safer than excluding apps that might actually work
            Timber.d("Could not determine notification access for $packageName: ${e.message}")
            true
        }
    }
    
    override suspend fun getAllApps(): Flow<List<App>> = flow {
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            // Get all app preferences from database
            val allPreferences = appPreferencesRepository.getAllPreferences()

            allPreferences.collect { preferences ->
                val preferencesMap = preferences.associateBy { it.packageName }

                val apps = packages.mapNotNull { packageInfo ->
                    try {
                        val applicationInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                        val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                        val packageName = packageInfo.packageName

                        // Skip our own app
                        if (packageName == context.packageName) {
                            return@mapNotNull null
                        }

                        // Filter out apps without notification access
                        if (!hasNotificationAccess(packageName)) {
                            Timber.d("Excluding $packageName - no notification access")
                            return@mapNotNull null
                        }

                        // Get forwarding status from database preferences
                        val isForwardingEnabled = preferencesMap[packageName]?.isForwardingEnabled ?: false

                        App(
                            packageName = packageName,
                            name = appName,
                            isSystemApp = isSystemApp,
                            isForwardingEnabled = isForwardingEnabled,
                            versionName = packageInfo.versionName ?: "",
                            versionCode = packageInfo.longVersionCode
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to get app info for ${packageInfo.packageName}")
                        null
                    }
                }

                // Initialize preferences for any new apps that don't have entries
                val appInfoList = apps.map {
                    AppInfo(
                        packageName = it.packageName,
                        appName = it.name,
                        isSystemApp = it.isSystemApp
                    )
                }
                appPreferencesRepository.initializePreferencesForApps(appInfoList)

                emit(apps)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get all apps")
            emit(emptyList())
        }
    }
    
    override suspend fun getAppsWithForwardingEnabled(): Flow<List<App>> = flow {
        try {
            val packageManager = context.packageManager

            // Get only enabled app preferences from database
            val enabledPreferences = appPreferencesRepository.getEnabledApps()

            enabledPreferences.collect { preferences ->
                val apps = preferences.mapNotNull { preference ->
                    try {
                        val packageInfo = packageManager.getPackageInfo(preference.packageName, PackageManager.GET_META_DATA)
                        val applicationInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                        val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

                        // Filter out apps without notification access (even if enabled)
                        if (!hasNotificationAccess(preference.packageName)) {
                            Timber.d("Excluding enabled app ${preference.packageName} - no notification access")
                            return@mapNotNull null
                        }

                        App(
                            packageName = preference.packageName,
                            name = appName,
                            isSystemApp = isSystemApp,
                            isForwardingEnabled = true, // These are all enabled apps
                            versionName = packageInfo.versionName ?: "",
                            versionCode = packageInfo.longVersionCode
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to get app info for enabled app ${preference.packageName}")
                        null
                    }
                }

                emit(apps)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get apps with forwarding enabled")
            emit(emptyList())
        }
    }
    
    override suspend fun updateAppForwardingStatus(packageName: String, isEnabled: Boolean): Result<Unit> {
        return try {
            Timber.d("Updating forwarding status for $packageName to $isEnabled")
            appPreferencesRepository.setForwardingEnabled(packageName, isEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update forwarding status for $packageName")
            Result.failure(e)
        }
    }
    
    override suspend fun getAppByPackageName(packageName: String): Result<App> {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            val applicationInfo = packageInfo.applicationInfo ?: throw IllegalStateException("ApplicationInfo is null")
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            // Get forwarding status from database
            val isForwardingEnabled = appPreferencesRepository.getForwardingStatus(packageName)

            val app = App(
                packageName = packageInfo.packageName,
                name = appName,
                isSystemApp = isSystemApp,
                isForwardingEnabled = isForwardingEnabled,
                versionName = packageInfo.versionName ?: "",
                versionCode = packageInfo.longVersionCode
            )

            Result.success(app)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get app by package name: $packageName")
            Result.failure(e)
        }
    }
    
    override suspend fun refreshInstalledApps(): Result<Unit> {
        return try {
            Timber.d("Refreshing installed apps")

            // Get current installed apps
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            val appInfoList = packages.mapNotNull { packageInfo ->
                try {
                    val applicationInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                    val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    val packageName = packageInfo.packageName

                    // Skip our own app
                    if (packageName == context.packageName) {
                        return@mapNotNull null
                    }

                    // Filter out apps without notification access
                    if (!hasNotificationAccess(packageName)) {
                        Timber.d("Excluding $packageName from refresh - no notification access")
                        return@mapNotNull null
                    }

                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        isSystemApp = isSystemApp
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get app info for ${packageInfo.packageName}")
                    null
                }
            }

            // Initialize preferences for any new apps
            appPreferencesRepository.initializePreferencesForApps(appInfoList)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh installed apps")
            Result.failure(e)
        }
    }
}
