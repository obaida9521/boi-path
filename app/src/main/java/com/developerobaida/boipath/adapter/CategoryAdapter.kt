package com.developerobaida.boipath.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.activity.CategoryWiseActivity
import com.developerobaida.boipath.databinding.ItemTopicsBinding
import com.developerobaida.boipath.model.CategoryModel

class CategoryAdapter(val list: List<CategoryModel>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopicsBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: CategoryModel){
            binding.topic.text = model.categoryName

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context,CategoryWiseActivity::class.java)
                intent.putExtra("category",model.categoryName)
                binding.root.context.startActivity(intent)
            }
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