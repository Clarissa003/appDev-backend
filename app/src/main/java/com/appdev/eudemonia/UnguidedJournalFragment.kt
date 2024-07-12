package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appdev.eudemonia.R
import com.appdev.eudemonia.authentication.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnguidedJournalFragment : Fragment() {

    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_unguided_journal, container, false)

        initializeViews(view)
        initializeFirebase()

        if (auth.currentUser == null) {
            navigateToLogin()
            return view
        }

        setupSaveButton()

        return view
    }

    private fun initializeViews(view: View) {
        editText = view.findViewById(R.id.enterDailyAnswer)
        saveButton = view.findViewById(R.id.saveButton)
    }

    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveJournalEntry()
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
        Toast.makeText(activity, "Entry saved!", Toast.LENGTH_SHORT).show()
        editText.text.clear()
    }

    private fun handleSaveFailure(errorMessage: String?) {
        Toast.makeText(activity, "Error saving entry: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun handleValidationErrors(content: String, userId: String?) {
        if (content.isEmpty()) {
            Toast.makeText(activity, "Content cannot be empty", Toast.LENGTH_SHORT).show()
        }
        if (userId == null) {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
