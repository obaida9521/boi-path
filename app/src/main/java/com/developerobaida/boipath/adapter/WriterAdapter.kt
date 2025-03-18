package com.developerobaida.boipath.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.R
import com.developerobaida.boipath.activity.WriterActivity
import com.developerobaida.boipath.databinding.ItemWriterBinding
import com.developerobaida.boipath.model.WriterModel
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class WriterAdapter(val list: List<WriterModel>) : RecyclerView.Adapter<WriterAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemWriterBinding) : RecyclerView.ViewHolder(binding.root){
        val langFormat = NumberFormat.getInstance(Locale("bn", "BD"))
        fun bind(model: WriterModel){
            if (model.image != null){
                Picasso.get().load(model.image).placeholder(R.drawable.dwewf).into(binding.writerImg)
            }else binding.writerImg.setImageResource(R.drawable.pic1)



            binding.name.text = model.name
            binding.followers.text = langFormat.format(model.followers)+" অনুসারী"
            binding.follow.setOnClickListener{
                Toast.makeText(binding.root.context,"Followed", Toast.LENGTH_SHORT).show()
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context,WriterActivity::class.java)
                intent.putExtra("author_id",model.id)
                binding.root.context.startActivity(intent)
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