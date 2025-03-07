package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R

class ChapterAdapter(private val chapters: List<String>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterTitle: TextView = itemView.findViewById(R.id.chapterTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.chapterTitle.text = "Chapter ${position + 1}"
        holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = chapters.size
}