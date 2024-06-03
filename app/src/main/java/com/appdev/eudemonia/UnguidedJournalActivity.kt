package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.LoginActivity
import com.appdev.eudemonia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class UnguidedJournalActivity : BaseActivity() {

    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unguided_journal)

        editText = findViewById(R.id.enterDailyAnswer)
        saveButton = findViewById(R.id.saveButton)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        if (auth.currentUser == null) {
            // Redirect to LoginActivity if not authenticated
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        saveButton.setOnClickListener {
            saveJournalEntry()
        }

        // Redirect to the unguided journal page
        findViewById<Button>(R.id.buttonUnguidedJournal).setOnClickListener {
            startActivity(Intent(this, UnguidedJournalActivity::class.java))
        }

        // Redirect to the guided journal page
        findViewById<Button>(R.id.buttonGuidedJournal).setOnClickListener {
            startActivity(Intent(this, GuidedJournalActivity::class.java))
        }

        // Redirect to the moods page
        findViewById<Button>(R.id.buttonMoods).setOnClickListener {
            startActivity(Intent(this, MoodsActivity::class.java))
        }

        // Redirect to the habits page
        findViewById<Button>(R.id.buttonHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
    }

    private fun saveJournalEntry() {
        val content = editText.text.toString().trim()
        val userId = auth.currentUser?.uid

        Log.d("UnguidedJournalActivity", "Content: $content")
        Log.d("UnguidedJournalActivity", "UserId: $userId")

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
                    editText.text.clear()
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