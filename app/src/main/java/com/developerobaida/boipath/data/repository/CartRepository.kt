package com.developerobaida.boipath.data.repository

import android.content.Context
import com.developerobaida.boipath.data.local.database.AppDatabase
import com.developerobaida.boipath.data.local.entity.CartEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(context: Context) {
    private val cartDao = AppDatabase.getDatabase(context).cartDao()

    suspend fun insertItem(cartItem: CartEntity) = withContext(Dispatchers.IO) {
        cartDao.insertItem(cartItem)
    }
    suspend fun updateItem(cartItem: CartEntity) = withContext(Dispatchers.IO){
        cartDao.updateItem(cartItem)
    }
    suspend fun deleteItem(id: Int) = cartDao.deleteItem(id)

    suspend fun getAllCartItems() = withContext(Dispatchers.IO){
        cartDao.getAllCartItems()
    }
    suspend fun clearCart() = cartDao.clearCart()
}