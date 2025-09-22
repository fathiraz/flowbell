package com.fathiraz.flowbell.data.local.database.converters

import androidx.room.TypeConverter
import com.fathiraz.flowbell.data.local.database.entities.NotificationQueueStatus

/**
 * Type converter for NotificationQueueStatus enum
 */
class NotificationQueueStatusConverter {
    
    @TypeConverter
    fun fromStatus(status: NotificationQueueStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toStatus(status: String): NotificationQueueStatus {
        return NotificationQueueStatus.valueOf(status)
    }
}