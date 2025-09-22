package com.fathiraz.flowbell.core.utils

import android.content.Context
import androidx.work.WorkManager

class NotificationWorkManager(private val context: Context) {
    fun initialize() {
        // Stub - initialize WorkManager workers
    }

    fun debugWorkManagerStatus() {
        // Stub - log WorkManager status
    }

    companion object {
        const val WORK_TAG = "notification_webhook"
    }
}