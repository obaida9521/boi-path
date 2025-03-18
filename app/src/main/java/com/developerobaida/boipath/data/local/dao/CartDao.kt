package com.developerobaida.boipath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.developerobaida.boipath.data.local.entity.CartEntity

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(cartItem: CartEntity)

    @Update
    suspend fun updateItem(cartItem: CartEntity)

    @Query("DELETE FROM cart_table WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("SELECT * FROM cart_table")
    suspend fun getAllCartItems(): List<CartEntity>

    @Query("DELETE FROM cart_table")
    suspend fun clearCart()
}