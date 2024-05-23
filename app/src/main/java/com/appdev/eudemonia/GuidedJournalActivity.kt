package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.databinding.ActivityGuidedJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class GuidedJournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuidedJournalBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuidedJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set click listener for the Generate Prompt button
        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }

        // Set click listener for the Save Entry button
        binding.saveButton.setOnClickListener {
            saveJournalEntry()
        }

        // Redirect to the unguided journal page
        findViewById<TextView>(R.id.buttonUnguidedJournal).setOnClickListener {
            startActivity(Intent(this, UnguidedJournalActivity::class.java))
        }

        // Redirect to the guided journal page
        findViewById<TextView>(R.id.buttonGuidedJournal).setOnClickListener {
            startActivity(Intent(this, GuidedJournalActivity::class.java))
        }

        // Redirect to the moods page
        findViewById<TextView>(R.id.buttonMoods).setOnClickListener {
            startActivity(Intent(this, MoodsActivity::class.java))
        }

        // Redirect to the habits page
        findViewById<TextView>(R.id.buttonHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
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

    private fun saveJournalEntry() {
        val content = binding.enterAnswer.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (content.isNotEmpty() && userId != null) {
            val journalEntry = hashMapOf(
                "content" to content,
                "date" to Date(),
                "userId" to userId
            )

            firestore.collection("Journal")
                .add(journalEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show()
                    binding.enterAnswer.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving entry: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            if (content.isEmpty()) {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show()
            }
            if (userId == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
