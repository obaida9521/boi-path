package com.developerobaida.boipath.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.developerobaida.boipath.data.local.dao.CartDao
import com.developerobaida.boipath.data.local.entity.CartEntity

@Database(entities = [CartEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "boi_path"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}