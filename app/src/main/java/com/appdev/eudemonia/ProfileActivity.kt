package com.appdev.eudemonia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileDisplayData: TextView

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePicture)
        profileName = findViewById(R.id.profileName)
        profileDisplayData = findViewById(R.id.profileDisplayData)

        loadUserProfile()

        profilePicture.setOnClickListener {
            selectImage()
        }

        // Redirect to the unguided journal page
        findViewById<TextView>(R.id.buttonUnguidedJournal).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Redirect to the guided journal page
        findViewById<TextView>(R.id.buttonGuidedJournal).setOnClickListener {
            startActivity(Intent(this, GuidedJournalActivity::class.java))
        }

        // Redirect to the moods page
        findViewById<TextView>(R.id.buttonMoods).setOnClickListener {
            startActivity(Intent(this, MoodsActivity::class.java))
        }

        // Redirect to the habits page
        findViewById<TextView>(R.id.buttonHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val profilePicUrl = currentUser.photoUrl
            profilePicUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .into(profilePicture)
            }
            profileName.text = currentUser.displayName ?: "No Username"
        } else {
            // Handle user not signed in
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImage.launch(intent)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    uploadImage(uri)
                }
            }
        }

    private fun uploadImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_pictures/$uid")

            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, now get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update FirebaseUser with the new photo URL
                        val user = auth.currentUser
                        user?.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
                            ?.addOnSuccessListener {
                                // Profile image URL updated successfully
                                // Reload the profile with the new image
                                loadUserProfile()
                                Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { exception ->
                                // Handle failed update
                                Toast.makeText(this, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failed image upload
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
