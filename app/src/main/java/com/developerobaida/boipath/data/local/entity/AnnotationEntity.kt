package com.developerobaida.boipath.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val fileName: String,
    val pageIndex: Int,
    val type: String, // "bookmark", "highlight", "note"
    val selectedText: String,
    val noteText: String? = null,
    val highlightColor: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)