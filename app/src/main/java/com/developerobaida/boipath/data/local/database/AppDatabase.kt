package com.developerobaida.boipath.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.developerobaida.boipath.data.local.dao.BookmarkDao
import com.developerobaida.boipath.data.local.entity.BookmarkEntity

@Database(entities = [BookmarkEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao


    val DB_NAME: String = "boi_path.db"

    private var instance: AppDatabase? = null

    @Synchronized
    fun getInstance(context: Context): AppDatabase? {
        if (instance == null) {
            instance = databaseBuilder<AppDatabase>(
                context.applicationContext,
                AppDatabase::class.java, DB_NAME
            ).fallbackToDestructiveMigration().build()
        }
        return instance
    }
}