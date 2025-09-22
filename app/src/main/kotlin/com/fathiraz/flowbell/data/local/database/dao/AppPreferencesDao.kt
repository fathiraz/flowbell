package com.fathiraz.flowbell.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.fathiraz.flowbell.data.local.database.entities.AppPreferences

@Dao
interface AppPreferencesDao {
    
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<AppPreferences>>
    
    @Query("SELECT * FROM app_preferences")
    suspend fun getAllPreferencesSync(): List<AppPreferences>
    
    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName")
    suspend fun getPreference(packageName: String): AppPreferences?
    
    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName")
    fun getPreferenceFlow(packageName: String): Flow<AppPreferences?>
    
    @Query("SELECT * FROM app_preferences WHERE isForwardingEnabled = 1")
    fun getEnabledApps(): Flow<List<AppPreferences>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: AppPreferences)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<AppPreferences>)
    
    @Update
    suspend fun updatePreference(preference: AppPreferences)
    
    @Delete
    suspend fun deletePreference(preference: AppPreferences)
    
    @Query("DELETE FROM app_preferences WHERE packageName = :packageName")
    suspend fun deletePreferenceByPackage(packageName: String)
    
    @Query("UPDATE app_preferences SET isForwardingEnabled = :enabled, updatedAt = :timestamp WHERE packageName = :packageName")
    suspend fun updateForwardingStatus(packageName: String, enabled: Boolean, timestamp: Long)
}
