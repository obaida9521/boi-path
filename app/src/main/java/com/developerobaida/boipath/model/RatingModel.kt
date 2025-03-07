package com.developerobaida.boipath.model

import com.google.gson.annotations.SerializedName

data class RatingModel(

    @SerializedName("id") val id: Int,
    @SerializedName("book_id") val bookId: Int,
    @SerializedName("rating") val rating: Float,
)
