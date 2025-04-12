package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R

class SectionsAdapter(
    private val sections: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    private var selectedPosition = -1

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = sections.size

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.section_title)

        fun bind(title: String, isSelected: Boolean) {
            titleTextView.text = title
            titleTextView.setTextColor(
                if (isSelected) {
                    ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark)
                } else {
                    ContextCompat.getColor(itemView.context, android.R.color.black)
                }
            )
        }
    }
}