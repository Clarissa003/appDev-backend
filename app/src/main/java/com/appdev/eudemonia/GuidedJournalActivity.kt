package com.appdev.eudemonia
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.databinding.ActivityGuidedJournalBinding


class GuidedJournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuidedJournalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuidedJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the Generate Prompt button
        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }
    }

    private fun generatePrompt() {
        // You'll need to replace "YOUR_API_KEY" with your actual Hugging Face API key
        val apiKey = "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"
        val inputs = "Reflect on your experiences from today and write about one thing you learned or discovered"

        // Call the Hugging Face API service to get the generated prompt
        HuggingFaceService().getGeneratedPrompt(apiKey, inputs) { prompt ->
            // Update the UI with the generated prompt
            binding.displayPrompt.text = prompt
        }
    }
}
