package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.databinding.ItemTopicsBinding
import com.developerobaida.boipath.model.TopicsModel

class TopicsAdapter(val list: List<TopicsModel>) : RecyclerView.Adapter<TopicsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopicsBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: TopicsModel){
            binding.topic.text = model.topic
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