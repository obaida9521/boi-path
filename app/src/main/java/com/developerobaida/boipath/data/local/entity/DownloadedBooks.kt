package com.developerobaida.boipath.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("books_table")
data class DownloadedBooks(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo("book_cover") val bookCover: String,
    @ColumnInfo("book_name") val bookName: String,
    @ColumnInfo("author") val author: String,
    @ColumnInfo("author_id") val authorId: Int,
    @ColumnInfo("pages") val pages: Int,
    @ColumnInfo("description") val description: String?,
    @ColumnInfo("categories") val categories: String,
    @ColumnInfo(name = "book_id") val bookId: Int
)