package com.appdev.eudemonia

import CompletionRequest
import CompletionResponse
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appdev.eudemonia.databinding.ActivityGuidedJournalBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GuidedJournalActivity : AppCompatActivity() {
    private lateinit var openAIInterface: OpenAIInterface
    private lateinit var retrofit: Retrofit
    //private val apiKey = "apiKey"
    private lateinit var binding: ActivityGuidedJournalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guided_journal)

        // Initialize Retrofit instance
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openAIInterface = retrofit.create(OpenAIInterface::class.java)

        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }
    }

    private fun generatePrompt() {
        val journalingPrompt = "Dear Journal, \n\n"
        val call = openAIInterface.getCompletion(
            "Bearer $apiKey",
            CompletionRequest(journalingPrompt, max_tokens = 100)
        )
        call.enqueue(object : Callback<CompletionResponse> {
            override fun onResponse(
                call: Call<CompletionResponse>,
                response: Response<CompletionResponse>
            ) {
                if (response.isSuccessful) {
                    val completion = response.body()?.choices?.firstOrNull()?.text
                    binding.displayPrompt.text = completion
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                showFailure()
            }
        })
    }

    private fun showError() {
        Toast.makeText(this, "Error occurred while fetching prompt", Toast.LENGTH_SHORT).show()
    }

    private fun showFailure() {
        Toast.makeText(this, "Failed to fetch prompt", Toast.LENGTH_SHORT).show()
    }
}

