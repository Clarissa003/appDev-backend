package com.appdev.eudemonia.journals

import android.os.Bundle
import android.widget.Toast
import com.appdev.eudemonia.base.BaseActivity
import com.appdev.eudemonia.services.HuggingFaceService
import com.appdev.eudemonia.databinding.ActivityGuidedJournalBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuidedJournalActivity : BaseActivity() {

    private lateinit var binding: ActivityGuidedJournalBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuidedJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        setupViews()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setupViews() {
        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }

        binding.saveButton.setOnClickListener {
            saveJournalEntry()
        }
    }

    private fun generatePrompt() {
        val apiKey = "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"
        val inputs = "Write about one positive thing that happened today."

        HuggingFaceService().getGeneratedPrompt(apiKey, inputs) { prompt ->
            binding.displayPrompt.text = prompt
        }
    }

    private fun saveJournalEntry() {
        val content = binding.enterAnswer.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (content.isNotEmpty() && userId != null) {
            val currentDate = dateFormat.format(Date())

            val journalEntry = hashMapOf(
                "content" to content,
                "date" to currentDate,
                "userId" to userId
            )

            firestore.collection("Journal")
                .add(journalEntry)
                .addOnSuccessListener {
                    handleSaveSuccess()
                }
                .addOnFailureListener { e ->
                    handleSaveFailure(e.message)
                }
        } else {
            handleValidationErrors(content, userId)
        }
    }

    private fun handleSaveSuccess() {
        Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show()
        binding.enterAnswer.text.clear()
    }

    private fun handleSaveFailure(errorMessage: String?) {
        Toast.makeText(this, "Error saving entry: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun handleValidationErrors(content: String, userId: String?) {
        if (content.isEmpty()) {
            Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show()
        }
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
