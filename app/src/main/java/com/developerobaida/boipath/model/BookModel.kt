package com.developerobaida.boipath.model
import com.google.gson.annotations.SerializedName

data class BookModel(
    @SerializedName("id") val id: Int,
    @SerializedName("book_cover") val bookCover: String,
    @SerializedName("epub_file") val epubFile: String,
    @SerializedName("book_name") val bookName: String,
    @SerializedName("author") val author: String,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("description") val description: String?,
    @SerializedName("total_sell") val totalSell: Int?,
    @SerializedName("published_date") val publishedDate: String?,
    @SerializedName("categories") val categories: List<BookCategoryModel> = emptyList()

)

data class BookCategoryModel(
    @SerializedName("id") val id: Int,
    @SerializedName("category_name") val name: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("pivot") val pivot: PivotModel?
)

data class PivotModel(
    @SerializedName("book_id") val bookId: Int,
    @SerializedName("category_id") val categoryId: Int
)
