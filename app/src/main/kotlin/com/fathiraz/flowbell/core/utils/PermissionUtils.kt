package com.fathiraz.flowbell.core.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import timber.log.Timber

object PermissionUtils {
    
    /**
     * Check if the Notification Listener Service permission is granted
     */
    fun isNotificationListenerPermissionGranted(context: Context): Boolean {
        val pkgName = context.packageName
        Timber.d("Checking notification listener permission for package: $pkgName")
        
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        
        Timber.d("Enabled notification listeners: $flat")
        
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            Timber.d("Found ${names.size} notification listeners")
            
            for (name in names) {
                Timber.d("Checking listener: $name")
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null) {
                    Timber.d("Component package: ${componentName.packageName}")
                    if (TextUtils.equals(pkgName, componentName.packageName)) {
                        Timber.d("Notification listener permission is GRANTED for $pkgName")
                        return true
                    }
                }
            }
        }
        
        Timber.d("Notification listener permission is NOT GRANTED for $pkgName")
        return false
    }
    
    /**
     * Open the system settings to grant Notification Listener permission
     */
    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Timber.d("Opened notification listener settings")
        } catch (e: Exception) {
            Timber.e(e, "Failed to open notification listener settings")
            // Fallback to general settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e2: Exception) {
                Timber.e(e2, "Failed to open general settings as fallback")
            }
        }
    }
}
