package com.developerobaida.boipath.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.ReviewAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.ActivityBookDetailsBinding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.RatingModel
import com.developerobaida.boipath.model.ReviewModel
import com.developerobaida.boipath.model.WriterModel
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BookDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding
    val apiController = ApiController.instance
    val apiInterface = apiController?.api
    val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bookId = intent.getIntExtra("bookId",0)
        fetchBookDetails(bookId)
        fetchReviews(bookId)

        binding.toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }


        binding.bookCover.setOnClickListener{
            val intent = Intent(this,WriterActivity::class.java)
            startActivity(intent)
        }

        binding.read.setOnClickListener{
            val intent = Intent(this,BookReaderActivity::class.java)
            startActivity(intent)
        }

        binding.submit.setOnClickListener{
            submitReview(bookId)
        }
        binding.rating.setOnClickListener{
            dialogRating(bookId)
        }

    }



    private fun fetchBookDetails(id: Int){
        apiInterface?.getBookById(id)?.enqueue(object : Callback<List<BookModel>>{
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful){
                    val book = response.body()
                    book?.let {
                        binding.bookName.text = book[0].bookName
                        binding.writer.text = book[0].author
                        binding.aboutBook.text = book[0].description

                        fetchAuthor(book[0].authorId)

                        binding.page.text = langFormat.format(book[0].pages)
                        binding.price.text = langFormat.format(book[0].price)+"৳"

                        if (book[0].bookCover !=null){
                            Picasso.get().load(book[0].bookCover).placeholder(R.drawable.pic1).into(binding.bookCover)
                        }else binding.bookCover.setImageResource(R.drawable.pic1)

                    }
                }else {
                    Log.e("API_ERROR", "Book Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Book Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<List<BookModel>>, p1: Throwable) {
                Log.e("API_ERROR", "Book Failed: ${p1.message}")
            }

        })
    }

    private fun fetchAuthor(id: Int){
        apiInterface?.getAuthorById(id)?.enqueue(object : Callback<WriterModel>{
            override fun onResponse(call: Call<WriterModel>, response: Response<WriterModel>) {
                if (response.isSuccessful){
                    val writer = response.body()
                    writer?.let {
                        binding.writerName2.text = writer.name
                        binding.writerFollower.text = langFormat.format(writer.followers)+" অনুসরণ কারী"
                        if (writer.image !=null){
                            Picasso.get().load(writer.image).into(binding.writerImg)
                        }else binding.writerImg.setImageResource(R.drawable.dwewf)
                    }
                }else{

                }
            }

            override fun onFailure(p0: Call<WriterModel>, p1: Throwable) {
                Log.e("API_ERROR",p1.message.toString())
            }

        })
    }

    private fun fetchReviews(bookId: Int){
        apiInterface?.getReviewsByBookId(bookId)?.enqueue(object : Callback<List<ReviewModel>> {
            override fun onResponse(call: Call<List<ReviewModel>>, response: Response<List<ReviewModel>>) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    reviews?.let {
                        Log.d("API_RESPONSE", "reviews: ${Gson().toJson(reviews)}")

                        val adapter = ReviewAdapter(reviews)
                        binding.reviewRec.adapter =adapter
                        binding.reviewRec.layoutManager = LinearLayoutManager(this@BookDetailsActivity, LinearLayoutManager.HORIZONTAL,false)
                        binding.reviewRec.hasFixedSize()
                    }
                } else {
                    Log.e("API_ERROR", "Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ReviewModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch books: ${t.message}")
            }
        })
    }

    private fun submitReview(bookId: Int) {
        val bookName = "Test name"
        val reviewBody = binding.inputReview.text.toString()
        val reviewerName = "a reviewer"
        val reviewerId = 1

        if (bookName.isEmpty() || reviewBody.isEmpty() || reviewerName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val call = apiInterface?.addReview(bookId.toString(), bookName, reviewBody, reviewerName, reviewerId.toString())
        call?.enqueue(object : Callback<ReviewModel> {
            override fun onResponse(call: Call<ReviewModel>, response: Response<ReviewModel>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BookDetailsActivity, "Review added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Failed to add review: $errorBody")
                    Toast.makeText(this@BookDetailsActivity, "Failed to add review: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewModel>, t: Throwable) {
                Toast.makeText(this@BookDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        /*apiInterface?.getCsrfToken()?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // CSRF token fetched successfully, now make the addReview request

                } else {
                    Toast.makeText(this@BookDetailsActivity, "Failed to fetch CSRF token", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BookDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })*/
    }



    fun addRating(bookId:Int,rating:Float){
        apiInterface?.addRatings(bookId.toString(),rating.toString())?.enqueue(object : Callback<RatingModel>{
            override fun onResponse(p0: Call<RatingModel>, p1: Response<RatingModel>) {
                if (p1.isSuccessful){
                    Toast.makeText(this@BookDetailsActivity,"Rating Successfully added!",Toast.LENGTH_SHORT).show()
                }else Log.e("API_ERROR", "Failed: ${p1.message()}")
            }

            override fun onFailure(p0: Call<RatingModel>, p1: Throwable) {
                Log.e("API_ERROR", "Failed: ${p1.message}")
            }

        })
    }

    private fun dialogRating(bookId: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating)
        val yes = dialog.findViewById<MaterialButton>(R.id.yes)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)


        yes.setOnClickListener { view: View? ->
            val rating = ratingBar.rating
            if (rating>0){
                addRating(bookId,rating)
            }else Toast.makeText(this,"Select the star",Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setWindowAnimations(R.style.dialogAnimation)
        dialog.create()
        dialog.show()
    }
}