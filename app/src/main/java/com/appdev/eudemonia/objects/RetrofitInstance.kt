package com.appdev.eudemonia.objects

import com.appdev.eudemonia.journals.HuggingFaceInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api-inference.huggingface.co/models/"

    val api: HuggingFaceInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceInterface::class.java)
    }
}