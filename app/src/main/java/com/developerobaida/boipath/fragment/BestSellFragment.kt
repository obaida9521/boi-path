package com.developerobaida.boipath.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.SelectionBottomSheet
import com.developerobaida.boipath.adapter.BestSellAdapter
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.FragmentBestSellBinding
import com.developerobaida.boipath.model.BookModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BestSellFragment : Fragment() {
    lateinit var binding: FragmentBestSellBinding
    private val apiService = ApiController.instance?.api


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBestSellBinding.inflate(inflater,container,false)

        binding.card.setOnClickListener {
            binding.card.setOnClickListener {
                val bottomSheet = SelectionBottomSheet { selectedText ->
                    binding.tvCat.text = selectedText
                    fetchBooks(selectedText)
                }
                bottomSheet.show(childFragmentManager, "SelectionBottomSheet")
            }
        }

        fetchBooks("islamic")

        return binding.root
    }


    private fun fetchBooks(category: String) {
        binding.tvCat.text = category

        apiService?.getBestSell(category)?.enqueue(object : Callback<List<BookModel>> {
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful) {
                    val books = response.body()
                    books?.let {
                        Log.d("API_RESPONSE", "Books: ${Gson().toJson(books)}")

                        val adapter = BestSellAdapter(books)
                        binding.bestSellRec.adapter = adapter
                        binding.bestSellRec.layoutManager = LinearLayoutManager(context)
                        binding.bestSellRec.hasFixedSize()
                    }
                } else {
                    Log.e("API_ERROR", "Response not successful: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<BookModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch books: ${t.message}")
            }
        })
    }
}