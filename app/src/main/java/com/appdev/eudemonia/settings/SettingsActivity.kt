package com.appdev.eudemonia.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.authentication.LoginActivity
import com.appdev.eudemonia.R
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        setupUI()
    }

    private fun setupUI() {
        val buttonNotificationTime = findViewById<Button>(R.id.buttonNotificationTime)
        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        val buttonDeleteAccount = findViewById<Button>(R.id.buttonDeleteAccount)

        buttonNotificationTime.setOnClickListener {
            navigateToTimeSelector()
        }

        buttonLogout.setOnClickListener {
            logout()
        }

        buttonDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }

    private fun navigateToTimeSelector() {
        val intent = Intent(this, TimeSelectorActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        showLogoutMessageAndNavigate()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showDeleteAccountMessageAndNavigate()
                } else {
                    Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLogoutMessageAndNavigate() {
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun showDeleteAccountMessageAndNavigate() {
        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
