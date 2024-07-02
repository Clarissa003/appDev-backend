package com.appdev.eudemonia.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.appdev.eudemonia.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var loginRedirectTextView: TextView
    private lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        initializeFirebase()
        initializeViews()
        initializeManagers()
        setButtonListeners()
    }

    private fun initializeFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.editTextEmail)
        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        loginRedirectTextView = findViewById(R.id.textViewLoginRedirect)
        signUpButton = findViewById(R.id.buttonSignUp)
    }

    private fun initializeManagers() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setButtonListeners() {
        signUpButton.setOnClickListener {
            handleSignUp()
        }

        loginRedirectTextView.setOnClickListener {
            redirectToLogin()
        }
    }

    private fun handleSignUp() {
        val email = emailEditText.text.toString()
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (validateInput(email, username, password, confirmPassword)) {
            signUpUser(email, username, password)
        }
    }

    private fun validateInput(email: String, username: String, password: String, confirmPassword: String): Boolean {
        return if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                true
            } else {
                confirmPasswordEditText.error = "Passwords do not match"
                false
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun signUpUser(email: String, username: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    user?.let {
                        saveUserToFirestore(it.uid, email, username)
                        saveProfileToFirestore(it.uid, username)
                    }
                } else {
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String, username: String) {
        val user = hashMapOf(
            "email" to email,
            "username" to username
        )

        firestore.collection("User")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to register user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileToFirestore(userId: String, username: String) {
        val profile = hashMapOf(
            "username" to username,
            "bio" to null,
            "profilePic" to null,
            "coverPhoto" to null
        )

        firestore.collection("Profile")
            .document(userId)
            .set(profile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
