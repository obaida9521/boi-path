package com.developerobaida.boipath.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiController internal constructor() {
    init {
        retrofit = Retrofit.Builder().baseUrl(URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val api: ApiInterface get() = retrofit.create(ApiInterface::class.java)

    companion object {
        private const val URL = "http://192.168.0.185:8000/"
       // private const val URL = "http://10.0.2.2:8000/"
        //private const val URL = "http://127.0.0.1:8000/"
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