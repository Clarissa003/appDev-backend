package com.appdev.eudemonia

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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
                resetPassword(email)
            }
        }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    Toast.makeText(
                        baseContext, "Password reset email sent.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // After sending the reset email, update the password
                    val newPassword = "YourNewPasswordHere"
                    updatePassword(newPassword)
                } else {
                    Log.e(TAG, "Failed to send reset email.", task.exception)
                    Toast.makeText(
                        baseContext, "Failed to send reset email. ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updatePassword(newPassword: String) {
        val user: FirebaseUser? = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Password updated successfully")
                    Toast.makeText(
                        baseContext, "Password updated successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, "Failed to update password", task.exception)
                    Toast.makeText(
                        baseContext, "Failed to update password. ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        private const val TAG = "ResetPasswordActivity"
    }
}
