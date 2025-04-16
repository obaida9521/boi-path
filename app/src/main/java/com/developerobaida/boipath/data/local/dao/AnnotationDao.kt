package com.developerobaida.boipath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.developerobaida.boipath.data.local.entity.AnnotationEntity

@Dao
interface AnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE bookId = :bookId")
    suspend fun getAnnotationsForBook(bookId: Int): List<AnnotationEntity>

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotation(id: Int)

    @Query("DELETE FROM annotations WHERE bookId = :bookId")
    suspend fun deleteAllAnnotationsForBook(bookId: Int)
}