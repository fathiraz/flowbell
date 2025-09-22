package com.fathiraz.flowbell.data.mappers

import com.fathiraz.flowbell.data.AppInfo
import com.fathiraz.flowbell.domain.entities.App

/**
 * Mapper for converting between data and domain App entities.
 */
object AppMapper {
    
    /**
     * Convert data AppInfo to domain App.
     */
    fun toDomain(appInfo: AppInfo): App {
        return App(
            packageName = appInfo.packageName,
            name = appInfo.appName,
            isForwardingEnabled = appInfo.isForwardingEnabled,
            isSystemApp = appInfo.isSystemApp,
            versionName = appInfo.versionName,
            versionCode = appInfo.versionCode
        )
    }
    
    /**
     * Convert domain App to data AppInfo.
     */
    fun toData(app: App): AppInfo {
        return AppInfo(
            packageName = app.packageName,
            appName = app.name,
            isForwardingEnabled = app.isForwardingEnabled,
            isSystemApp = app.isSystemApp,
            versionName = app.versionName,
            versionCode = app.versionCode
        )
    }
}