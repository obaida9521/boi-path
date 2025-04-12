package com.developerobaida.boipath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.developerobaida.boipath.data.local.entity.DownloadedBooks

@Dao
interface DownloadedBooksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(book: DownloadedBooks)

    @Update
    suspend fun updateItem(book: DownloadedBooks)

    @Query("DELETE FROM books_table WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("SELECT * FROM books_table")
    suspend fun getAllItems(): List<DownloadedBooks>

    @Query("SELECT * FROM books_table WHERE book_id =:id")
    suspend fun getBookById(id: Int): DownloadedBooks

    @Query("DELETE FROM books_table")
    suspend fun clearCart()
}