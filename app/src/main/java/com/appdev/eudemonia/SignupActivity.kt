package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class SignupActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth if not already initialized
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Find views
        emailEditText = findViewById(R.id.editTextEmail)
        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        signUpButton = findViewById(R.id.buttonSignUp)

        // Set click listener for sign up button
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Basic validation
            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Passwords match
                    // Create user with email and password
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user = auth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build()

                                user?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { profileTask ->
                                        if (profileTask.isSuccessful) {
                                            // Add user to Firestore
                                            addUserToFirestore(user!!.uid, email, username, password)
                                            // Show success message
                                            Toast.makeText(
                                                applicationContext,
                                                "User registered successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Clear input fields
                                            emailEditText.text.clear()
                                            usernameEditText.text.clear()
                                            passwordEditText.text.clear()
                                            confirmPasswordEditText.text.clear()
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Failed to update user profile",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    applicationContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Passwords don't match, show error message
                    confirmPasswordEditText.error = "Passwords do not match"
                }
            } else {
                // Fields are empty, show error message
                Toast.makeText(applicationContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        val textViewLoginRedirect: TextView = findViewById(R.id.textViewLoginRedirect)
        textViewLoginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity if you don't want it to stay in the back stack
        }
    }

    private fun addUserToFirestore(userId: String, email: String, username: String, password: String) {
        // Hash the password
        val hashedPassword = hashString(password)

        // Create user object
        val user = hashMapOf(
            "email" to email,
            "name" to username,
            "password" to hashedPassword
        )

        // Add user to Firestore
        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                // User added successfully to Firestore
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to register user in Firestore: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Function to hash the password using SHA-256
    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

}
