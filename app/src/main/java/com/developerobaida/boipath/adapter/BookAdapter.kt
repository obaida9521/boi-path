package com.developerobaida.boipath.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.BookDetailsActivity
import com.developerobaida.boipath.databinding.ItemBook1Binding
import com.developerobaida.boipath.model.BookModel
import com.developerobaida.boipath.model.Constant.BASE_URL
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class BookAdapter(val itemList: List<BookModel>) : RecyclerView.Adapter<BookAdapter.BookView>() {

    class BookView(private val binding: ItemBook1Binding) :RecyclerView.ViewHolder(binding.root){
        val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))
        fun bind(book: BookModel) {
            binding.bookName.text = book.bookName
            binding.writer.text = book.author
            binding.price.text = langFormat.format(book.price)+"$"

            if (book.bookCover!=null){
                Picasso.get().load(BASE_URL+"storage/"+book.bookCover).placeholder(R.drawable.pic1).into(binding.bookCover)
            } else binding.bookCover.setImageResource(R.drawable.pic1)

            binding.card.setOnClickListener {
                val intent = Intent(binding.root.context,BookDetailsActivity::class.java)
                intent.putExtra("bookId",book.id)
                binding.root.context.startActivity(intent)
            }

            binding.bookCover.setOnClickListener {
                val intent = Intent(binding.root.context,BookDetailsActivity::class.java)
                intent.putExtra("bookId",book.id)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookView {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBook1Binding.inflate(inflater, parent, false)
        return BookView(binding)
    }

    override fun getItemCount(): Int =itemList.size

    override fun onBindViewHolder(holder: BookView, position: Int) {
        holder.bind(itemList[position])
    }
}