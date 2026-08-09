// ============================================================
// FILE: app/src/main/java/com/example/data/db/FocusDatabase.kt
// PURPOSE: Room database instance for local storage.
// CREATED: 2026-08-09
// ============================================================

package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FocusDao
import com.example.data.entity.*

@Database(
    entities = [
        BlockedAppEntity::class,
        FocusSessionEntity::class,
        TaskEntity::class,
        CalendarEventEntity::class,
        StreakGoalEntity::class,
        ThemeSettingsEntity::class,
        YoutubeWhitelistEntity::class,
        WebsiteBlockEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {

    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getDatabase(context: Context): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_forge_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
