package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegisterRedirect: TextView
    private lateinit var textViewForgotPassword: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegisterRedirect = findViewById(R.id.textViewRegisterRedirect)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        textViewRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        textViewForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun login(username: String, password: String) {
        usersCollection.whereEqualTo("username", username).get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents[0]
                val user = userDocument.data
                if (user != null) {
                    val hashedPasswordFromDB = user["password"] as? String
                    if (hashedPasswordFromDB != null) {
                        val hashedPasswordInput = hashPassword(password)
                        if (hashedPasswordFromDB == hashedPasswordInput) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Password field not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}
