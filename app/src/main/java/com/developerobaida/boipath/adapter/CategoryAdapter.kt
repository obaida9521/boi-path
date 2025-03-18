package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.databinding.ItemTopicsBinding
import com.developerobaida.boipath.model.CategoryModel

class CategoryAdapter(val list: List<CategoryModel>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopicsBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: CategoryModel){
            binding.topic.text = model.categoryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTopicsBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}