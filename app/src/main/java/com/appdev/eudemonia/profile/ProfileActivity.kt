package com.appdev.eudemonia.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var coverPhoto: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileDisplayData: TextView
    private lateinit var shareButton: ImageButton
    private lateinit var profileBio: TextView

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePicture)
        coverPhoto = findViewById(R.id.coverPhoto)
        profileName = findViewById(R.id.profileName)
        profileDisplayData = findViewById(R.id.profileBio)
        shareButton = findViewById(R.id.shareButton)
        profileBio = findViewById(R.id.profileBio)

        loadUserProfile()

        profilePicture.setOnClickListener {
            selectImage("profilePicture")
        }

        coverPhoto.setOnClickListener {
            selectImage("coverPhoto")
        }

        findViewById<Button>(R.id.editProfileNameButton).setOnClickListener {
            showEditDialog("name")
        }

        findViewById<Button>(R.id.editBio).setOnClickListener {
            showEditDialog("bio")
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

            db.collection("Profile").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        profileBio.text = document.getString("bio") ?: "No bio available"
                        val coverPhotoUrl = document.getString("coverPhoto")
                        coverPhotoUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .into(coverPhoto)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting profile bio: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showEditDialog(field: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit $field")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val text = input.text.toString()
            if (field == "name") {
                updateProfileName(text)
            } else if (field == "bio") {
                updateProfileBio(text)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun updateProfileName(name: String) {
        val user = auth.currentUser
        user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    profileName.text = name
                    Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()

                    db.collection("Profile").document(user.uid)
                        .update("username", name)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Name updated in Profile collection", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error updating name in Profile collection: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }

                    db.collection("User").document(user.uid)
                        .update("username", name)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Name updated in Users collection", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error updating name in Users collection: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    db.collection("Friends").document(user.uid)
                        .update("username", name)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Name updated in Friends collection", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error updating name in Friends collection: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun updateProfileBio(bio: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("Profile").document(it.uid)
                .update("bio", bio)
                .addOnSuccessListener {
                    profileBio.text = bio
                    Toast.makeText(this, "Bio updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error updating bio: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun selectImage(imageType: String) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (imageType == "profilePicture") {
            pickProfileImage.launch(intent)
        } else if (imageType == "coverPhoto") {
            pickCoverImage.launch(intent)
        }
    }

    private val pickProfileImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    uploadImage(uri, "profilePicture")
                }
            }
        }

    private val pickCoverImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    uploadImage(uri, "coverPhoto")
                }
            }
        }

    private fun uploadImage(imageUri: Uri, imageType: String) {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val storageRef = storage.reference
            val imageRef = if (imageType == "profilePicture") {
                storageRef.child("profile_pictures/$uid")
            } else {
                storageRef.child("cover_photos/$uid")
            }

            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val user = auth.currentUser
                        if (imageType == "profilePicture") {
                            user?.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
                                ?.addOnSuccessListener {
                                    loadUserProfile()
                                    Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                                    db.collection("Profile").document(uid)
                                        .update("profilePic", uri.toString())
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Profile image updated in Firestore", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(this, "Error updating profile image in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                ?.addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else if (imageType == "coverPhoto") {
                            db.collection("Profile").document(uid)
                                .update("coverPhoto", uri.toString())
                                .addOnSuccessListener {
                                    loadUserProfile()
                                    Toast.makeText(this, "Cover photo updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Error updating cover photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }
}
