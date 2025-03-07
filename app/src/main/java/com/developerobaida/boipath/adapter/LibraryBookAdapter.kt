package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.databinding.ItemBook2Binding
import com.developerobaida.boipath.model.BookModel
import com.squareup.picasso.Picasso

class LibraryBookAdapter(val list: List<BookModel>) : RecyclerView.Adapter<LibraryBookAdapter.BookView>() {

    class BookView(private val binding: ItemBook2Binding) :RecyclerView.ViewHolder(binding.root){
        fun bind(book: BookModel) {
//            binding.bookName.text = book.bookName
//            binding.writer.text = book.author
//
//            if (book.bookCover!=null){
//                Picasso.get().load(book.bookCover).placeholder(R.drawable.pic1).into(binding.bookCover)
//            } else binding.bookCover.setImageResource(R.drawable.pic1)

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