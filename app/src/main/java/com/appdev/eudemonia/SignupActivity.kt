package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
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
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
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
                    // Create a user object
                    val user = hashMapOf(
                        "email" to email,
                        "name" to username,
                        "password" to password
                    )

                    // Add user to Firestore
                    addUserToFirestore(user)
                } else {
                    // Passwords don't match, show error message
                    confirmPasswordEditText.error = "Passwords do not match"
                }
            } else {
                // Fields are empty, show error message
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        val textViewLoginRedirect: TextView = findViewById(R.id.textViewLoginRedirect)
        textViewLoginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity if you don't want it to stay in the back stack
        }
    }

    private fun addUserToFirestore(user: HashMap<String, String>) {
        // Hash the password before adding the user to Firestore
        val hashedPassword = hashPassword(user["password"]!!)
        user["password"] = hashedPassword

        // Add user to Firestore
        firestore.collection("User")
            .add(user)
            .addOnSuccessListener { documentReference ->
                // Handle success
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle failure
                Toast.makeText(this, "Failed to register user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}
