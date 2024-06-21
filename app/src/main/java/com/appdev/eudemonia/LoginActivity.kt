package com.appdev.eudemonia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegisterRedirect: TextView
    private lateinit var textViewForgotPassword: TextView
    private lateinit var auth: FirebaseAuth
    private val myPreferences: SharedPreferences by lazy {
        getSharedPreferences("myPref", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Check if UID is already stored in SharedPreferences
        val uid = myPreferences.getString("uid", null)
        if (uid != null) {
            // UID exists, check last login time
            val lastLoginTime = myPreferences.getLong("lastLoginTime", 0L)
            val currentTime = Calendar.getInstance().timeInMillis
            val hoursPassed = (currentTime - lastLoginTime) / (1000 * 60 * 60)

            if (hoursPassed < 24) {
                // Less than 24 hours passed, navigate to HomeActivity
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return
            } else {
                // More than 24 hours passed, clear stored UID and prompt for login
                clearSharedPreferences()
            }
        }

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegisterRedirect = findViewById(R.id.textViewRegisterRedirect)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)

        // Set click listeners
        buttonLogin.setOnClickListener {
            val email = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        textViewRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        textViewForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Store UID and current time in SharedPreferences
                    val uid = user?.uid
                    uid?.let {
                        myPreferences.edit().putString("uid", it).apply()
                        myPreferences.edit().putLong("lastLoginTime", Calendar.getInstance().timeInMillis).apply()
                    }

                    // Navigate to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: finish the LoginActivity to prevent going back
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Authentication failed. ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun clearSharedPreferences() {
        // Clear stored UID and last login time
        myPreferences.edit().remove("uid").apply()
        myPreferences.edit().remove("lastLoginTime").apply()
    }
}
