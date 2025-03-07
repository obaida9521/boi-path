package com.developerobaida.boipath.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.developerobaida.boipath.api.ApiController
import com.developerobaida.boipath.databinding.ItemReviewBinding
import com.developerobaida.boipath.model.ReviewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewAdapter(val list: List<ReviewModel>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>(){


    class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root){
        val apiController = ApiController.instance
        val apiInterface = apiController?.api
        fun bind(model: ReviewModel){

            binding.reviewer.text = model.reviewerName
            binding.reviewBody.text = model.reviewBody

        }

         fun deleteReview(id: Int){
            apiInterface?.deleteReview(id)?.enqueue(object : Callback<ReviewModel> {
                override fun onResponse(p0: Call<ReviewModel>, p1: Response<ReviewModel>) {
                }

                override fun onFailure(p0: Call<ReviewModel>, p1: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }


}