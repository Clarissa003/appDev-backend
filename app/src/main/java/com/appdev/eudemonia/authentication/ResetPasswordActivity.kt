package com.appdev.eudemonia.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseAuth()
        setButtonListeners()
    }

    private fun initializeFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun setButtonListeners() {
        binding.buttonReset.setOnClickListener {
            handleResetPassword()
        }

        binding.textViewLoginRedirect.setOnClickListener {
            redirectToLogin()
        }

        binding.textViewRegisterRedirect.setOnClickListener {
            redirectToSignup()
        }
    }

    private fun handleResetPassword() {
        val email = binding.editTextEmail.text.toString().trim()

        if (validateEmail(email)) {
            sendResetPasswordEmail(email)
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            Toast.makeText(
                baseContext, "Please enter your email address.",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun redirectToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    private fun sendResetPasswordEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleResetEmailSuccess()
                } else {
                    handleResetEmailFailure()
                }
            }
            .addOnFailureListener {
                handleResetEmailFailure()
            }
    }

    private fun handleResetEmailSuccess() {
        Toast.makeText(this, "Reset email sent", Toast.LENGTH_LONG).show()
        redirectToLogin()
        finish()
    }

    private fun handleResetEmailFailure() {
        Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_LONG).show()
    }
}
