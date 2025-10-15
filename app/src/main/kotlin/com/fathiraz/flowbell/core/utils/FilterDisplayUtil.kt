package com.fathiraz.flowbell.core.utils

/**
 * Utility functions for displaying filter information in a consistent and compact format
 */
object FilterDisplayUtil {
    
    /**
     * Formats a list of filter words to display a maximum of 10 words,
     * with a "+X more" indicator if there are additional words.
     * 
     * @param filters List of filter words to format
     * @return Formatted string showing up to 10 words plus count of remaining words
     */
    fun formatActiveFilters(filters: List<String>): String {
        return if (filters.size <= 10) {
            filters.joinToString(", ")
        } else {
            val displayWords = filters.take(10).joinToString(", ")
            val remaining = filters.size - 10
            "$displayWords +$remaining more"
        }
    }
}
