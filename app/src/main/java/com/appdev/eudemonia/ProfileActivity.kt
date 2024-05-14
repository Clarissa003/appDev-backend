package com.appdev.eudemonia

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileDisplayData: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePicture)
        profileName = findViewById(R.id.profileName)
        profileDisplayData = findViewById(R.id.profileDisplayData)

        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            loadUserProfile(userId)
        } else {
            profileDisplayData.text = "Error: User ID is null"
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                val email = document.getString("email") ?: "No Email"
                val profileId = document.getString("profileId")

                profileDisplayData.text = email

                profileId?.let {
                    firestore.collection("Profile").document(it).get()
                        .addOnSuccessListener { profileDoc ->
                            val username = profileDoc.getString("username") ?: "No Username"
                            profileName.text = username

                            val profilePicPath = profileDoc.getString("profilePic")
                            profilePicPath?.let { path ->
                                loadProfilePicture(path)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
                profileDisplayData.text = "Error fetching data"
            }
    }

    private fun loadProfilePicture(profilePicPath: String) {
        val storageRef = storage.getReference(profilePicPath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(profilePicture)
        }.addOnFailureListener {
            // Handle any errors here
            profilePicture.setImageResource(R.drawable.default_profile_picture)
        }
    }
}

