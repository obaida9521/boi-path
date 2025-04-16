package com.developerobaida.boipath.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.BookReaderActivity
import com.developerobaida.boipath.data.local.entity.DownloadedBooks
import com.developerobaida.boipath.databinding.ItemBook2Binding
import com.developerobaida.boipath.model.Constant.BASE_URL
import com.squareup.picasso.Picasso

class LibraryBookAdapter(val list: List<DownloadedBooks>) : RecyclerView.Adapter<LibraryBookAdapter.BookView>() {

    class BookView(private val binding: ItemBook2Binding) :RecyclerView.ViewHolder(binding.root){
        fun bind(book: DownloadedBooks) {
            binding.bookName.text = book.bookName
            binding.writer.text = book.author


            Picasso.get().load(BASE_URL+"storage/"+book.bookCover).placeholder(R.drawable.place_holder_book).error(R.drawable.place_holder_book).into(binding.bookCover)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context,BookReaderActivity::class.java)
                intent.putExtra("bookId",book.bookId)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookView {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBook2Binding.inflate(inflater, parent, false)
        return BookView(binding)
    }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: BookView, position: Int) {
        holder.bind(list[position])
    }
}