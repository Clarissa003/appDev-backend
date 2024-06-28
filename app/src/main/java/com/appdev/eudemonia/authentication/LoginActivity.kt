package com.appdev.eudemonia.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegisterRedirect: TextView
    private lateinit var textViewForgotPassword: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeFirebaseAuth()
        initializeViews()
        setButtonListeners()
    }

    private fun initializeFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initializeViews() {
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegisterRedirect = findViewById(R.id.textViewRegisterRedirect)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
    }

    private fun setButtonListeners() {
        buttonLogin.setOnClickListener {
            handleLogin()
        }

        textViewRegisterRedirect.setOnClickListener {
            redirectToSignup()
        }

        textViewForgotPassword.setOnClickListener {
            redirectToResetPassword()
        }
    }

    private fun handleLogin() {
        val email = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        if (validateInput(email, password)) {
            login(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return if (email.isNotEmpty() && password.isNotEmpty()) {
            true
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun redirectToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    private fun redirectToResetPassword() {
        startActivity(Intent(this, ResetPasswordActivity::class.java))
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleLoginSuccess()
                } else {
                    handleLoginFailure(task.exception?.message)
                }
            }
    }

    private fun handleLoginSuccess() {
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleLoginFailure(errorMessage: String?) {
        Toast.makeText(this, "Authentication failed. $errorMessage", Toast.LENGTH_SHORT).show()
    }
}