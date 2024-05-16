package com.appdev.eudemonia

import CompletionRequest
import CompletionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIInterface {
    @POST("completions")
    fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: CompletionRequest
    ): Call<CompletionResponse>
}
