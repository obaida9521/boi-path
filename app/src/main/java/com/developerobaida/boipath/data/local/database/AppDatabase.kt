package com.developerobaida.boipath.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.developerobaida.boipath.data.local.dao.AnnotationDao
import com.developerobaida.boipath.data.local.dao.CartDao
import com.developerobaida.boipath.data.local.dao.DownloadedBooksDao
import com.developerobaida.boipath.data.local.entity.AnnotationEntity
import com.developerobaida.boipath.data.local.entity.CartEntity
import com.developerobaida.boipath.data.local.entity.DownloadedBooks

@Database(entities = [CartEntity::class,DownloadedBooks::class, AnnotationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao
    abstract fun booksDao(): DownloadedBooksDao
    abstract fun annotationDao(): AnnotationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "boi_path"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}