package com.fathiraz.flowbell.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey
    val packageName: String,
    val isForwardingEnabled: Boolean = false,
    @ColumnInfo(name = "filter_words")
    val filterWords: String = "", // Stored as comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
