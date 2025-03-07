package com.developerobaida.boipath.model

import com.google.gson.annotations.SerializedName

data class WriterModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name:String,
    @SerializedName("image") val image: String,
    @SerializedName("total_books") val totalBooks: Int,
    @SerializedName("followers") val followers: Int
)
