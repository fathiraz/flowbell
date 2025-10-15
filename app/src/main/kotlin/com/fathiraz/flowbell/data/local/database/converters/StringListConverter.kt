package com.fathiraz.flowbell.data.local.database.converters

import androidx.room.TypeConverter

/**
 * TypeConverter for converting between List<String> and comma-separated String
 * Used for storing filter words in database
 */
class StringListConverter {
    
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
    
    @TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}
