package com.fathiraz.flowbell.core.utils

import timber.log.Timber

/**
 * Utility for filtering notifications based on word filters
 * Implements whitelist filtering: only allow notifications containing filter words
 */
object NotificationFilterUtil {
    
    /**
     * Determines if a notification should be allowed based on filter words
     * 
     * @param title Notification title
     * @param text Notification text content
     * @param appFilterWords Per-app filter words (takes priority if not empty)
     * @param globalFilterWords Global filter words (fallback if app filters empty)
     * @return true if notification should be allowed, false if filtered out
     */
    fun shouldAllowNotification(
        title: String,
        text: String,
        appFilterWords: List<String>,
        globalFilterWords: List<String>
    ): Boolean {
        // Use app filters if available, otherwise global
        val activeFilters = if (appFilterWords.isNotEmpty()) {
            Timber.d("🔍 Using app-specific filters: ${appFilterWords.joinToString(",")}")
            appFilterWords
        } else {
            Timber.d("🔍 Using global filters: ${globalFilterWords.joinToString(",")}")
            globalFilterWords
        }
        
        // If no filters, allow all
        if (activeFilters.isEmpty()) {
            Timber.d("✅ No filters set - allowing notification")
            return true
        }
        
        // Whitelist: allow if ANY filter word found (case-insensitive, partial match)
        val content = "$title $text".lowercase()
        val hasMatch = activeFilters.any { filterWord ->
            val normalizedFilter = filterWord.lowercase().trim()
            if (normalizedFilter.isEmpty()) return@any false
            
            val found = content.contains(normalizedFilter)
            if (found) {
                Timber.d("✅ Filter match found: '$normalizedFilter' in notification content")
            }
            found
        }
        
        if (hasMatch) {
            Timber.d("✅ Notification allowed - contains filter word")
        } else {
            Timber.d("🚫 Notification filtered - no filter words found in: '$title' | '$text'")
        }
        
        return hasMatch
    }
}
