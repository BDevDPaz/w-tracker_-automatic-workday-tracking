package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BreakSegment
import com.example.data.model.DestinationVisit
import com.example.data.model.SavedDestination
import com.example.data.model.TimelineEvent
import com.example.data.model.TransferSegment
import com.example.data.model.UserPreferences
import com.example.data.model.WalLogEntry
import com.example.data.model.WorkSession

@Database(
    entities = [
        WorkSession::class,
        DestinationVisit::class,
        TransferSegment::class,
        BreakSegment::class,
        TimelineEvent::class,
        UserPreferences::class,
        WalLogEntry::class,
        SavedDestination::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workSessionDao(): WorkSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wtracker_v2.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
