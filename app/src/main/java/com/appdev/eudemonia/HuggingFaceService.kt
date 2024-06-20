package com.appdev.eudemonia

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class HuggingFaceService {

    fun getGeneratedPrompt(apiKey: String, inputs: String, callback: (String) -> Unit) {
        val request = HuggingFaceRequest(inputs = inputs)
        val call = RetrofitInstance.api.getCompletion("Bearer $apiKey", request)

        call.enqueue(object : Callback<List<HuggingFaceResponse>> {
            override fun onResponse(call: Call<List<HuggingFaceResponse>>, response: Response<List<HuggingFaceResponse>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("HuggingFaceService", "Response body: $responseBody")
                    val generatedText = extractGeneratedText(responseBody)
                    Log.d("HuggingFaceService", "Extracted Text: $generatedText")
                    callback(generatedText)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("HuggingFaceService", "Error body: $errorBody")
                    callback("Failed to load prompt.")
                }
            }

            override fun onFailure(call: Call<List<HuggingFaceResponse>>, t: Throwable) {
                Log.e("HuggingFaceService", "API call failed", t)
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