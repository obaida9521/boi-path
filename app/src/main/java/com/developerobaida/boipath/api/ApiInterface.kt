package com.developerobaida.boipath.api

import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.CategoryModel
import com.developerobaida.boipath.model.RatingModel
import com.developerobaida.boipath.model.ReviewModel
import com.developerobaida.boipath.model.WriterModel
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    //============  Books  =======================  Books  ====================  Books  =============================  Books  ===================||||||
    @GET("api/books")
    fun getBooks(): Call<List<BookModel>>

    @GET("api/books/{id}")
    fun getBookById(@Path("id") id: Int): Call<List<BookModel>>


    @GET("api/books/search/{book_name}")
    fun searchBook(@Path("book_name") bookName: String): Call<List<BookModel>>

    @GET("api/books/author/{author_id}")
    fun getBookByWriterId(@Path("author_id") id: Int): Call<List<BookModel>>

    @GET("api/books/best_sell/{category}")
    fun getBestSell(@Path("category") category: String): Call<List<BookModel>>



    //============  Authors  =======================  Authors  ====================  Authors  =============================  Authors  ===================||||||
    @GET("api/authors")
    fun getAuthors(): Call<List<WriterModel>>

    @GET("api/authors/{id}")
    fun getAuthorById(@Path("id") id: Int): Call<WriterModel>


    //============  Review  =======================  Review  ====================  Review  =============================  Review  ===================||||||

    @GET("sanctum/csrf-cookie")
    fun getCsrfToken(): Call<Void>

    @GET("api/reviews")
    fun getReviews(): Call<List<ReviewModel>>

    @GET("api/reviews/book/{book_id}")
    fun getReviewsByBookId(@Path("book_id") bookId: Int ): Call<List<ReviewModel>>

    @POST("api/reviews")
    @FormUrlEncoded
    fun addReview(
        @Field("book_id") bookId: String,
        @Field("book_name") bookName: String,
        @Field("review_body") reviewBody: String,
        @Field("reviewer_name") reviewerName: String,
        @Field("reviewer_id") reviewerId: String
    ): Call<ReviewModel>

    @DELETE("api/reviews/{id}")
    fun deleteReview(@Path("id") id: Int): Call<ReviewModel>


    //============  Ratings  =======================  Ratings  ====================  Ratings  =============================  Ratings  ===================||||||

    @GET("api/ratings/book/{book_id}")
    fun getRatingsByBookId(@Path("book_id") bookId: Int ): Call<List<RatingModel>>

    @POST("api/ratings")
    @FormUrlEncoded
    fun addRatings(
        @Field("book_id") bookId: String,
        @Field("rating") rating: String
    ): Call<RatingModel>


    //============  Category  =======================  Category  ====================  Category  =============================  Category  ===================||||||

    @GET("api/category")
    fun getCategories(): Call<List<CategoryModel>>

}
