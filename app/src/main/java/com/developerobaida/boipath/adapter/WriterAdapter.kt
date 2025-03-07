package com.developerobaida.boipath.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.databinding.ItemWriterBinding
import com.developerobaida.boipath.model.WriterModel
import com.squareup.picasso.Picasso

class WriterAdapter(val list: List<WriterModel>) : RecyclerView.Adapter<WriterAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemWriterBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: WriterModel){
            if (model.image != null){
                Picasso.get().load(model.image).placeholder(R.drawable.dwewf).into(binding.writerImg)
            }else binding.writerImg.setImageResource(R.drawable.pic1)

            binding.name.text = model.name
            binding.followers.text = model.followers.toString()
            binding.follow.setOnClickListener{
                Toast.makeText(binding.root.context,"Followed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWriterBinding.inflate(inflater,parent,false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}