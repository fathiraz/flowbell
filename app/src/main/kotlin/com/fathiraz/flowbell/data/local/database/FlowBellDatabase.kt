package com.fathiraz.flowbell.data.local.database

import androidx.room.Database
import androidx.room.TypeConverters
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.fathiraz.flowbell.data.local.database.entities.UserPreferencesEntity
import com.fathiraz.flowbell.data.local.database.entities.AppPreferences
import com.fathiraz.flowbell.data.local.database.entities.NotificationQueue
import com.fathiraz.flowbell.data.local.database.converters.NotificationQueueStatusConverter
import com.fathiraz.flowbell.data.local.database.dao.UserPreferencesDao
import com.fathiraz.flowbell.data.local.database.dao.AppPreferencesDao
import com.fathiraz.flowbell.data.local.database.dao.NotificationQueueDao
import com.fathiraz.flowbell.core.utils.LoggerUtils

@Database(
    entities = [AppPreferences::class, NotificationQueue::class, UserPreferencesEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(NotificationQueueStatusConverter::class)
abstract class FlowBellDatabase : RoomDatabase() {

    abstract fun appPreferencesDao(): AppPreferencesDao
    abstract fun notificationQueueDao(): NotificationQueueDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    companion object {
        @Volatile
        private var INSTANCE: FlowBellDatabase? = null

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create notification_queue table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        appName TEXT NOT NULL,
                        title TEXT NOT NULL,
                        text TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        iconUri TEXT,
                        largeIconUri TEXT,
                        isOngoing INTEGER NOT NULL DEFAULT 0,
                        isClearable INTEGER NOT NULL DEFAULT 1,
                        priority INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        createdAt INTEGER NOT NULL,
                        retryCount INTEGER NOT NULL DEFAULT 0,
                        lastAttemptAt INTEGER,
                        errorMessage TEXT
                    )
                """.trimIndent())
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LoggerUtils.Database.i("Migrating database from version 2 to 3: Adding user_preferences table")

                // Create user_preferences table for reliable webhook storage
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_preferences (
                        id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
                        is_first_launch INTEGER NOT NULL DEFAULT 1,
                        notifications_enabled INTEGER NOT NULL DEFAULT 1,
                        webhook_url TEXT NOT NULL DEFAULT '',
                        auto_start_service INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        updated_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Insert default preferences row
                database.execSQL("""
                    INSERT OR REPLACE INTO user_preferences (
                        id, theme_mode, is_first_launch, notifications_enabled,
                        webhook_url, auto_start_service, created_at, updated_at
                    ) VALUES (
                        1, 'SYSTEM', 1, 1, '', 0,
                        ${System.currentTimeMillis()}, ${System.currentTimeMillis()}
                    )
                """.trimIndent())

                LoggerUtils.Database.i("User preferences table created successfully")
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LoggerUtils.Database.i("Migrating database from version 3 to 4: Adding is_onboarding_completed column")

                try {
                    // Add the is_onboarding_completed column to user_preferences table
                    database.execSQL("""
                        ALTER TABLE user_preferences
                        ADD COLUMN is_onboarding_completed INTEGER NOT NULL DEFAULT 0
                    """.trimIndent())

                    LoggerUtils.Database.i("is_onboarding_completed column added successfully")
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error adding is_onboarding_completed column", e)
                    throw e
                }
            }
        }

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LoggerUtils.Database.i("Migrating database from version 4 to 5: Adding app_preferences table")

                try {
                    // Create app_preferences table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS app_preferences (
                            packageName TEXT PRIMARY KEY NOT NULL,
                            isForwardingEnabled INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL DEFAULT 0,
                            updatedAt INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent())

                    LoggerUtils.Database.i("app_preferences table created successfully")
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error creating app_preferences table", e)
                    throw e
                }
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LoggerUtils.Database.i("Migrating database from version 5 to 6: Adding HTTP details to notification_queue")

                try {
                    // Add HTTP details columns to notification_queue table
                    database.execSQL("""
                        ALTER TABLE notification_queue
                        ADD COLUMN httpUrl TEXT
                    """.trimIndent())

                    database.execSQL("""
                        ALTER TABLE notification_queue
                        ADD COLUMN httpMethod TEXT
                    """.trimIndent())

                    database.execSQL("""
                        ALTER TABLE notification_queue
                        ADD COLUMN httpResponseCode INTEGER
                    """.trimIndent())

                    database.execSQL("""
                        ALTER TABLE notification_queue
                        ADD COLUMN httpResponseBody TEXT
                    """.trimIndent())

                    database.execSQL("""
                        ALTER TABLE notification_queue
                        ADD COLUMN httpDuration INTEGER
                    """.trimIndent())

                    LoggerUtils.Database.i("HTTP details columns added to notification_queue successfully")
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error adding HTTP details columns", e)
                    throw e
                }
            }
        }

        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LoggerUtils.Database.i("Migrating database from version 6 to 7: Adding performance indices")

                try {
                    // Create indices for better query performance
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_created_at ON notification_queue(createdAt)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_status ON notification_queue(status)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_status_created_at ON notification_queue(status, createdAt)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_app_name ON notification_queue(appName)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_package_name ON notification_queue(packageName)")

                    LoggerUtils.Database.i("Performance indices created successfully")
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error creating performance indices", e)
                    throw e
                }
            }
        }

        fun getDatabase(context: Context): FlowBellDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowBellDatabase::class.java,
                    "flowbell_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Database callback for handling initialization and setup
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                LoggerUtils.Database.i("FlowBell database created from scratch")

                // Ensure user_preferences table exists and has default row
                ensureUserPreferencesTable(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                LoggerUtils.Database.d("FlowBell database opened - ensuring user preferences exist")

                // Always ensure user preferences row exists on every open
                ensureUserPreferencesTable(db)
            }

            private fun ensureUserPreferencesTable(db: SupportSQLiteDatabase) {
                try {
                    // Check if user_preferences table exists
                    val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='user_preferences'")
                    val tableExists = cursor.count > 0
                    cursor.close()

                    if (!tableExists) {
                        LoggerUtils.Database.i("Creating user_preferences table")
                        // Create the table if it doesn't exist
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS user_preferences (
                                id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                                theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
                                is_first_launch INTEGER NOT NULL DEFAULT 1,
                                notifications_enabled INTEGER NOT NULL DEFAULT 1,
                                webhook_url TEXT NOT NULL DEFAULT '',
                                auto_start_service INTEGER NOT NULL DEFAULT 0,
                                is_onboarding_completed INTEGER NOT NULL DEFAULT 0,
                                created_at INTEGER NOT NULL DEFAULT 0,
                                updated_at INTEGER NOT NULL DEFAULT 0
                            )
                        """.trimIndent())
                    } else {
                        // Table exists, check if is_onboarding_completed column exists
                        ensureOnboardingColumn(db)
                    }

                    // Check if the default row exists
                    val rowCursor = db.query("SELECT COUNT(*) FROM user_preferences WHERE id = 1")
                    rowCursor.moveToFirst()
                    val rowExists = rowCursor.getInt(0) > 0
                    rowCursor.close()

                    if (!rowExists) {
                        LoggerUtils.Database.i("Inserting default user preferences row")
                        // Insert default preferences row
                        db.execSQL("""
                            INSERT OR REPLACE INTO user_preferences (
                                id, theme_mode, is_first_launch, notifications_enabled,
                                webhook_url, auto_start_service, is_onboarding_completed,
                                created_at, updated_at
                            ) VALUES (
                                1, 'SYSTEM', 1, 1, '', 0, 0,
                                ${System.currentTimeMillis()}, ${System.currentTimeMillis()}
                            )
                        """.trimIndent())
                        LoggerUtils.Database.i("Default user preferences inserted successfully")
                    } else {
                        LoggerUtils.Database.d("User preferences row already exists")
                    }
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error ensuring user preferences table/row", e)
                }
            }

            private fun ensureOnboardingColumn(db: SupportSQLiteDatabase) {
                try {
                    // Check if is_onboarding_completed column exists
                    val columnCursor = db.query("PRAGMA table_info(user_preferences)")
                    var columnExists = false
                    while (columnCursor.moveToNext()) {
                        val columnName = columnCursor.getString(1) // Column name is at index 1
                        if (columnName == "is_onboarding_completed") {
                            columnExists = true
                            break
                        }
                    }
                    columnCursor.close()

                    if (!columnExists) {
                        LoggerUtils.Database.i("Adding missing is_onboarding_completed column")
                        db.execSQL("""
                            ALTER TABLE user_preferences
                            ADD COLUMN is_onboarding_completed INTEGER NOT NULL DEFAULT 0
                        """.trimIndent())
                        LoggerUtils.Database.i("is_onboarding_completed column added successfully")
                    }
                } catch (e: Exception) {
                    LoggerUtils.Database.e("Error ensuring onboarding column", e)
                }
            }
        }
    }
}
