package com.fathiraz.flowbell.domain.entities

/**
 * Domain entity representing an installed application.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class App(
    val packageName: String,
    val name: String,
    val isForwardingEnabled: Boolean = false,
    val isSystemApp: Boolean = false,
    val versionName: String? = null,
    val versionCode: Long = 0
)