package com.developerobaida.boipath.api

import com.developerobaida.boipath.model.Constant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiController internal constructor() {
    init {
        retrofit = Retrofit.Builder().baseUrl(Constant.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val api: ApiInterface get() = retrofit.create(ApiInterface::class.java)

    companion object {
        private var controller: ApiController? = null
        private lateinit var retrofit: Retrofit

        @get:Synchronized
        val instance: ApiController?
            get() {
                if (controller == null) controller = ApiController()
                return controller
            }
    }
}