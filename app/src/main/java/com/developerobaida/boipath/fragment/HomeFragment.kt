package com.developerobaida.boipath.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.SearchActivity
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.adapter.CategoryAdapter
import com.developerobaida.boipath.adapter.WriterAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.FragmentHomeBinding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.CategoryModel
import com.developerobaida.boipath.model.WriterModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var view: List<TextView>
    private val apiService = ApiController.instance?.api

    private val ANIMATION_DURATION = 300L
    private val allBooks = "সকল বই"
    private val topics = "বিষয় সমূহ"
    private val writers = "সকল লেখক"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        val tabText = "$allBooks,$topics,$writers";
        view = tabController(binding.root.context, 3, tabText, binding.tab.itemLay, binding.tab.indicator)

        fetchBooks()
        fetchWriters()
        getCategories()

        binding.tab.indicator.viewTreeObserver.addOnGlobalLayoutListener {
            if (view.isNotEmpty()) {
                val firstTab = view[0]
                val tabWidth = firstTab.width
                val params = binding.tab.indicator.layoutParams
                params.width = tabWidth
                binding.tab.indicator.layoutParams = params
            }
        }

        binding.search.setOnClickListener {
            val intent = Intent(context,SearchActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    private fun getCategories(){
        apiService?.getCategories()?.enqueue(object : Callback<List<CategoryModel>>{
            override fun onResponse(
                p0: Call<List<CategoryModel>>,
                p1: Response<List<CategoryModel>>
            ) {
                if (p1.isSuccessful){
                    val category = p1.body()
                    category?.let {
                        val  adapter3 = CategoryAdapter(category)
                        binding.categoryRec.adapter =adapter3
                        binding.categoryRec.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
                        binding.categoryRec.hasFixedSize()
                    }
                }else {
                    Log.e(
                        "API_ERROR",
                        "Category Response not successful: ${p1.code()} ${p1.message()}"
                    )
                    Log.e("API_ERROR", "Category Error body: ${p1.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<List<CategoryModel>>, p1: Throwable) {
                Log.e("API_ERROR", "Failed to fetch Category: ${p1.message}")
            }

        })
    }

    private fun fetchWriters() {
        apiService?.getAuthors()?.enqueue(object : Callback<List<WriterModel>> {
            override fun onResponse(
                call: Call<List<WriterModel>>,
                response: Response<List<WriterModel>>
            ) {
                if (response.isSuccessful) {
                    val writers = response.body()

                    writers?.let {
                        val adapter2 = WriterAdapter(writers)
                        binding.writersRec.adapter = adapter2
                        binding.writersRec.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        binding.writersRec.hasFixedSize()
                    }
                } else {
                    Log.e(
                        "API_ERROR",
                        "Author Response not successful: ${response.code()} ${response.message()}"
                    )
                    Log.e("API_ERROR", "Author Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<WriterModel>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch Author: ${t.message}")
            }
        })
    }

    private fun fetchBooks() {

        apiService?.getBooks()?.enqueue(object : Callback<List<BookModel>> {
            override fun onResponse(call: Call<List<BookModel>>, response: Response<List<BookModel>>) {
                if (response.isSuccessful) {
                    val books = response.body()
                    books?.let {
                        Log.d("API_RESPONSE", "Books: ${Gson().toJson(books)}")

                        val adapter = BookAdapter(books)
                        binding.rec.adapter =adapter
                        binding.rec.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
                        binding.rec.hasFixedSize()
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


    private fun tabController(
        context: Context,
        count: Int,
        text: String,
        itemLay: LinearLayout,
        indicator: View
    ): ArrayList<TextView> {
        val textViewList = ArrayList<TextView>()
        val parts = text.split(",")

        for (i in 0 until count) {
            val textView = TextView(context).apply {
                setText(if (i < parts.size) parts[i] else "")
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                gravity = Gravity.CENTER
                setTextColor(if (i == 0) Color.WHITE else ContextCompat.getColor(context, R.color.orange))
            }

            textView.setOnClickListener {
                handleTabChange(textViewList, textView, indicator)
                //Handler().postDelayed({}, 300)

                when (textView.text.toString()) {
                    allBooks -> {
                        binding.allBooksLay.visibility = View.VISIBLE
                        binding.topics.visibility = View.GONE
                        binding.writers.visibility = View.GONE
                    }
                    topics -> {
                        binding.allBooksLay.visibility = View.GONE
                        binding.topics.visibility = View.VISIBLE
                        binding.writers.visibility = View.GONE
                    }
                    writers -> {
                        binding.allBooksLay.visibility = View.GONE
                        binding.topics.visibility = View.GONE
                        binding.writers.visibility = View.VISIBLE
                    }
                }
            }

            textViewList.add(textView)
            itemLay.addView(textView)
        }
        return textViewList
    }

    private fun handleTabChange(textViewList: ArrayList<TextView>, selectedTextView: TextView, indicator: View) {
        val selectedIndex = textViewList.indexOf(selectedTextView)
        if (selectedIndex != -1) {
            textViewList.forEach { it.setTextColor(ContextCompat.getColor(it.context, R.color.orange)) }
            selectedTextView.setTextColor(Color.WHITE)

            val translationX = selectedTextView.left.toFloat()
            indicator.animate().translationX(translationX)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator()).start()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}