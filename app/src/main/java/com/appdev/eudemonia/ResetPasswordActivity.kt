package com.appdev.eudemonia

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

        auth = FirebaseAuth.getInstance()

        binding.buttonReset.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(
                    baseContext, "Please enter your email address.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendResetPasswordEmail(email)
            }
        }

        binding.textViewLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.textViewRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun sendResetPasswordEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset email sent", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Finish the current activity
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@ResetPasswordActivity, "Error Occurred", Toast.LENGTH_LONG
                ).show()
            }
    }
}
