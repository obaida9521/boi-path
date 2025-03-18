package com.developerobaida.boipath.adapter

import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.BookDetailsActivity
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.ItemBook3Binding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.RatingModel
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale


class BestSellAdapter(val list: List<BookModel>) : RecyclerView.Adapter<BestSellAdapter.BookView>() {
    val apiService = ApiController.instance?.api

    class BookView(private val binding: ItemBook3Binding) :RecyclerView.ViewHolder(binding.root){
        val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))

        fun bind(book: BookModel, showRating: (Int, (Float,Int) -> Unit) -> Unit) {
            binding.price2.paintFlags = binding.price2.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            binding.bookName.text = book.bookName
            binding.writer.text = book.author

            val price = langFormat.format(book.price)
            binding.price.text = "$price ৳"

            binding.ranking.text = "#${(position+1)}"

            Picasso.get().load(book.bookCover).placeholder(R.drawable.pic6).error(R.drawable.pic6).into(binding.bookCover)


            showRating(book.id) { rating,ratingCount ->

                binding.ratingView.rating = rating

                val count = langFormat.format(ratingCount)
                binding.ratingCount.text = "($count)"
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, BookDetailsActivity::class.java)
                intent.putExtra("bookId",book.id)
                binding.root.context.startActivity(intent)
            }
        }
    }

    private fun showRating(bookId: Int, callback: (Float,Int) -> Unit) {
        apiService?.getRatingsByBookId(bookId)?.enqueue(object : Callback<List<RatingModel>> {
            override fun onResponse(p0: Call<List<RatingModel>>, p1: Response<List<RatingModel>>) {
                if (p1.isSuccessful) {
                    val ratings = p1.body()
                    val avgRating = if (!ratings.isNullOrEmpty()) {
                        ratings.sumOf { it.rating.toDouble() }.toFloat() / ratings.size
                    } else 0F

                    callback(avgRating,ratings!!.size)
                }
            }

            override fun onFailure(p0: Call<List<RatingModel>>, p1: Throwable) {
                Log.e("API_ERROR", "Rating: ${p1.message}")
                callback(0F,0)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookView {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBook3Binding.inflate(inflater, parent, false)
        return BookView(binding)
    }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: BookView, position: Int) {
        holder.bind(list[position],::showRating)
    }
}