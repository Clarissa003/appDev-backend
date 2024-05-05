package com.appdev.eudemonia
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("User")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(username: String, password: String) {
        usersCollection.whereEqualTo("name", username).get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents[0]
                val user = userDocument.data
                if (user != null) {
                    val hashedPasswordFromDB = user["password"] as? String // Retrieve hashed password from database
                    if (hashedPasswordFromDB != null) {
                        val hashedPasswordInput = hashPassword(password) // Hash the password entered by the user
                        if (hashedPasswordFromDB == hashedPasswordInput) {
                            // Passwords match, proceed with login
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            // Proceed to next activity or do whatever you want
                        } else {
                            // Passwords don't match
                            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Password field not found in user document
                        Toast.makeText(this, "Password field not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // User document not found
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Error retrieving user document
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
