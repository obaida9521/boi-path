package com.developerobaida.boipath.data.repository

import android.content.Context
import com.developerobaida.boipath.data.local.database.AppDatabase
import com.developerobaida.boipath.data.local.entity.DownloadedBooks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadedBookRepository (context: Context){
    private val booksDao = AppDatabase.getDatabase(context).booksDao()

    suspend fun insertBook(book: DownloadedBooks) = withContext(Dispatchers.IO) {
        booksDao.insertItem(book)
    }
    suspend fun updateBook(cartItem: DownloadedBooks) = withContext(Dispatchers.IO){
        booksDao.updateItem(cartItem)
    }
    suspend fun deleteBook(id: Int) = booksDao.deleteItem(id)

    suspend fun getAllBooks() = withContext(Dispatchers.IO){
        booksDao.getAllItems()
    }

    suspend fun getBookById(id: Int) = withContext(Dispatchers.IO){
        booksDao.getBookById(id)
    }

    suspend fun clearBooks() = booksDao.clearCart()
}