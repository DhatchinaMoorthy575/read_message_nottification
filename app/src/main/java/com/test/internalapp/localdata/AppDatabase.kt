package com.test.internalapp.localdata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.test.internalapp.localdata.dao.MessageDao
import com.test.internalapp.localdata.dao.StatusBarNotificationDao
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity

@Database(entities = [StatusBarNotificationEntity::class, MessageEntity::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun statusBarNotificationDao(): StatusBarNotificationDao

    abstract fun MessageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}