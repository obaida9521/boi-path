package com.developerobaida.boipath.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "bookName") val bookName: String?,
    @ColumnInfo(name = "coverImgUrl") val coverImgUrl: String?,
    @ColumnInfo(name = "writer") val writer: String?
)