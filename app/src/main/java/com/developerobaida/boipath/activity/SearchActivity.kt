package com.developerobaida.boipath.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.BestSellAdapter
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.ActivitySearchBinding
import com.developerobaida.boipath.model.BookModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    private val apiService = ApiController.instance?.api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.search.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT)

        binding.search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId === EditorInfo.IME_ACTION_DONE || actionId === EditorInfo.IME_ACTION_NEXT || actionId === EditorInfo.IME_ACTION_SEARCH) {
                val text: String = binding.search.text.toString().trim()
                if (text.isNotEmpty()) {
                    searchBooks(text)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

    }

    private fun searchBooks(text: String) {

        apiService?.searchBook(text)?.enqueue(object : Callback<List<BookModel>> {
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful) {
                    val books = response.body()
                    books?.let {
                        Log.d("API_RESPONSE", "Books: ${Gson().toJson(books)}")

                        val adapter = BestSellAdapter(books)
                        binding.searchRec.adapter =adapter
                        binding.searchRec.layoutManager = LinearLayoutManager(this@SearchActivity)
                        binding.searchRec.hasFixedSize()
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