package com.developerobaida.boipath.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.ActivityWriterBinding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.Constant.BASE_URL
import com.developerobaida.boipath.model.WriterModel
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class WriterActivity : AppCompatActivity() {
    lateinit var binding: ActivityWriterBinding
    private val api = ApiController.instance?.api
    private val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getIntExtra("author_id",0);
        fetchBook(id)
        fetchAuthor(id)

    }

    private fun fetchAuthor(id: Int){
        api?.getAuthorById(id)?.enqueue(object : Callback<WriterModel>{
            override fun onResponse(call: Call<WriterModel>, response: Response<WriterModel>) {
                if (response.isSuccessful){
                    val writer = response.body()
                    writer?.let {
                        binding.writerName.text = writer.name
                        binding.followers.text = langFormat.format(writer.followers)+" ফলোয়ার"
                        binding.aboutAuthor.text =writer.about
                        binding.bookCount.text = langFormat.format(writer.totalBooks)+" টি বই"

                        if (writer.image !=null){
                            Picasso.get().load(BASE_URL+"storage/"+writer.image).error(R.drawable.user).into(binding.writerImg)
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

    private fun fetchBook(id: Int){
        api?.getBookByWriterId(id)?.enqueue(object : Callback<List<BookModel>>{
            override fun onResponse(p0: Call<List<BookModel>>, p1: Response<List<BookModel>>) {
                if (p1.isSuccessful){

                    val bookList = p1.body()
                    bookList?.let {
                        val adapter = BookAdapter(bookList)
                        binding.rec.adapter =adapter
                        binding.rec.layoutManager = LinearLayoutManager(this@WriterActivity, LinearLayoutManager.HORIZONTAL,false)
                    }
                }else Toast.makeText(this@WriterActivity,"Unsuccessful",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Call<List<BookModel>>, p1: Throwable) {
                Toast.makeText(this@WriterActivity,"Failed",Toast.LENGTH_SHORT).show()
            }
        })
    }
}