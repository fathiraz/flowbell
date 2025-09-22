package com.fathiraz.flowbell.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isForwardingEnabled: Boolean = false,
    val isSystemApp: Boolean = false,
    val versionName: String? = null,
    val versionCode: Long = 0
)
