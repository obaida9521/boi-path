package com.developerobaida.boipath.model

import com.google.gson.annotations.SerializedName

data class Purchase(
    @SerializedName("uid") val uid: String,
    @SerializedName("book_id") val bookId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("payment_status") val paymentStatus: String
)
