package com.developerobaida.boipath.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerobaida.boipath.R
import com.developerobaida.boipath.adapter.BookAdapter
import com.developerobaida.boipath.databinding.ActivityWriterBinding
import com.developerobaida.boipath.model.BookModel

class WriterActivity : AppCompatActivity() {
    lateinit var binding: ActivityWriterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val bookList = listOf(
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"",""),
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"",""),
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"",""),
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"",""),
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"",""),
            BookModel("", "", "রুপার্ট মারডক : ভয়ানক ইসরায়েল প্রেমের গল্প","খুবাইব মাহমুদ",1,11,10,"","")
        )

        val adapter = BookAdapter(bookList)
        binding.rec.adapter =adapter
        binding.rec.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)*/

    }
}