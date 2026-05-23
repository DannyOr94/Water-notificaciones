package com.watersf.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.watersf.app.data.local.dao.NotificationDao
import com.watersf.app.data.local.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
