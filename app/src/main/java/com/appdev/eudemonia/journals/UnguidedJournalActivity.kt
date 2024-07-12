package com.appdev.eudemonia.journals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.authentication.LoginActivity
import com.appdev.eudemonia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnguidedJournalActivity : BaseActivity() {

    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var goToGuidedJournalButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unguided_journal)

        initializeViews()
        initializeFirebase()
        setupSaveButton()
        setupGoToGuidedJournalButton()
    }

    private fun initializeViews() {
        editText = findViewById(R.id.enterDailyAnswer)
        saveButton = findViewById(R.id.saveButton)
        goToGuidedJournalButton = findViewById(R.id.gotoGuidedJournalButton)
    }

    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveJournalEntry()
        }
    }
    private fun setupGoToGuidedJournalButton() {
        goToGuidedJournalButton.setOnClickListener {
            val intent = Intent(this, GuidedJournalActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveJournalEntry() {
        val content = editText.text.toString().trim()
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
        editText.text.clear()
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
