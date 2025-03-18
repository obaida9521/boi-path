package com.developerobaida.boipath.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "book_name") val bookName: String,
    @ColumnInfo(name = "book_cover") val bookCover: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "pages") val pages: Int,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "categories") val categories: String,
    @ColumnInfo(name = "book_id") val bookId: Int
)