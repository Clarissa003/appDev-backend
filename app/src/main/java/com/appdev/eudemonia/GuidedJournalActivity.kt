package com.appdev.eudemonia

import HuggingFaceRequest
import HuggingFaceResponse
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appdev.eudemonia.databinding.ActivityGuidedJournalBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuidedJournalActivity : AppCompatActivity() {
    private lateinit var huggingFaceInterface: HuggingFaceInterface
    private lateinit var retrofit: Retrofit
    private val apiKey = "Bearer " // Replace with your actual API key
    private lateinit var binding: ActivityGuidedJournalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guided_journal)

        // Initialize Retrofit instance
        retrofit = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/models/openai-community/") // Ensure the base URL ends with /
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        huggingFaceInterface = retrofit.create(HuggingFaceInterface::class.java)

        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }
    }

    private fun generatePrompt() {
        val journalingPrompt = "Dear Journal, \n\n"
        val call = huggingFaceInterface.getCompletion(
            apiKey,
            HuggingFaceRequest(inputs = journalingPrompt)
        )
        call.enqueue(object : Callback<HuggingFaceResponse> {
            override fun onResponse(
                call: Call<HuggingFaceResponse>,
                response: Response<HuggingFaceResponse>
            ) {
                if (response.isSuccessful) {
                    val completion = response.body()?.generated_text
                    binding.displayPrompt.text = completion ?: "No response from API"
                } else {
                    showError(response.message())
                }
            }

            override fun onFailure(call: Call<HuggingFaceResponse>, t: Throwable) {
                showFailure(t.message)
            }
        })
    }

    private fun showError(message: String?) {
        Toast.makeText(this, "Error occurred while fetching prompt: $message", Toast.LENGTH_SHORT).show()
    }

    private fun showFailure(message: String?) {
        Toast.makeText(this, "Failed to fetch prompt: $message", Toast.LENGTH_SHORT).show()
    }
}
