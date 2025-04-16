package com.developerobaida.boipath.adapter

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.BookDetailsActivity
import com.developerobaida.boipath.data.local.entity.CartEntity
import com.developerobaida.boipath.databinding.ItemCartBinding
import com.squareup.picasso.Picasso

class AdapterCart(
    private var list: List<CartEntity>,
    private val listener: OnDeleteClickListener
) : RecyclerView.Adapter<AdapterCart.MyHolder>() {

    inner class MyHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: CartEntity) {
            binding.price2.paintFlags = binding.price2.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.bookName.text = model.bookName
            binding.writer.text = model.author
            binding.price.text = model.price.toString()
            if (model.bookCover.isNotEmpty()){
                Picasso.get().load(model.bookCover).placeholder(R.drawable.place_holder_book).error(R.drawable.place_holder_book)
                    .into(binding.bookCover)
            }

            binding.del.setOnClickListener {
                model.id?.let { it1 -> listener.onDeleteClick(adapterPosition, it1) }
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, BookDetailsActivity::class.java)
                intent.putExtra("bookId",model.bookId)
                binding.root.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCartBinding.inflate(inflater, parent, false)
        return MyHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.bind(list[position])
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int,id: Int)
    }
}