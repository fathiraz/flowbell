package com.fathiraz.flowbell.domain.entities

/**
 * Domain entity representing app information.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isForwardingEnabled: Boolean = false,
    val isSystemApp: Boolean = false,
    val versionName: String? = null,
    val versionCode: Long = 0
)
