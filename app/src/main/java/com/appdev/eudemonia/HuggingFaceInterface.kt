package com.appdev.eudemonia

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HuggingFaceInterface {
    @POST("gpt2")
    fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: HuggingFaceRequest
    ): Call<List<HuggingFaceResponse>>
}
