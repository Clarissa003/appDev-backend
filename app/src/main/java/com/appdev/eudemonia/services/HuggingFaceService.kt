package com.appdev.eudemonia.services

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.appdev.eudemonia.dataclasses.HuggingFaceRequest
import com.appdev.eudemonia.dataclasses.HuggingFaceResponse
import com.appdev.eudemonia.objects.RetrofitInstance

class HuggingFaceService {

    fun getGeneratedPrompt(apiKey: String, inputs: String, callback: (String) -> Unit) {
        val request = HuggingFaceRequest(inputs = inputs)
        val call = RetrofitInstance.api.getCompletion("Bearer $apiKey", request)

        call.enqueue(object : Callback<List<HuggingFaceResponse>> {
            override fun onResponse(call: Call<List<HuggingFaceResponse>>, response: Response<List<HuggingFaceResponse>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val generatedText = extractGeneratedText(responseBody)
                    callback(generatedText)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback("Failed to load prompt.")
                }
            }

            override fun onFailure(call: Call<List<HuggingFaceResponse>>, t: Throwable) {
                callback("Error: ${t.message}")
            }
        })
    }

    private fun extractGeneratedText(responseList: List<HuggingFaceResponse>?): String {
        return if (responseList.isNullOrEmpty()) {
            "No prompt generated."
        } else {
            responseList.joinToString("\n\n") { it.generatedText }
        }
    }
}