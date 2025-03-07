package com.developerobaida.boipath.model

import com.google.gson.annotations.SerializedName

data class ReviewModel(
    @SerializedName("id") val id: Int,
    @SerializedName("book_id") val bookId: String,
    @SerializedName("book_name") val bookName: String,
    @SerializedName("review_body") val reviewBody: String,
    @SerializedName("reviewer_name") val reviewerName: String,
    @SerializedName("reviewer_id") val reviewerId: String,
)