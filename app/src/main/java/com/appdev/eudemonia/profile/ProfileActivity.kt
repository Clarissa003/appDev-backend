package com.appdev.eudemonia.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.authentication.LoginActivity
import com.appdev.eudemonia.settings.SettingsActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var coverPhoto: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileBio: TextView
    private lateinit var editProfileNameButton: Button
    private lateinit var editBioButton: Button
    private lateinit var settingsButton: Button


    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setupClickListeners()

        loadUserProfile()
    }

    private fun initializeViews() {
        profilePicture = findViewById(R.id.profilePicture)
        coverPhoto = findViewById(R.id.coverPhoto)
        profileName = findViewById(R.id.profileName)
        profileBio = findViewById(R.id.profileBio)
        editProfileNameButton = findViewById(R.id.editProfileNameButton)
        editBioButton = findViewById(R.id.editBio)
        settingsButton = findViewById(R.id.settingsButton)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupClickListeners() {
        profilePicture.setOnClickListener { selectImage("profilePicture") }
        coverPhoto.setOnClickListener { selectImage("coverPhoto") }
        editProfileNameButton.setOnClickListener { showEditDialog("name") }
        editBioButton.setOnClickListener { showEditDialog("bio") }
        settingsButton.setOnClickListener { navigateToSettings() }
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val profilePicUrl = user.photoUrl
            profilePicUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .into(profilePicture)
            }

            db.collection("Profile").document(user.uid).get()
                .addOnSuccessListener { document ->
                    document?.let {
                        profileBio.text = it.getString("bio") ?: "No bio available"
                        profileName.text = it.getString("username") ?: "No Username"
                        val coverPhotoUrl = it.getString("coverPhoto")
                        coverPhotoUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .into(coverPhoto)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting profile data: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            when (field) {
                "name" -> updateProfileName(text)
                "bio" -> updateProfileBio(text)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun updateProfileName(name: String) {
        val user = auth.currentUser
        user?.let { currentUser ->
            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    profileName.text = name
                    Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                    updateProfileInFirestore(currentUser.uid, "username", name)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update name: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateProfileBio(bio: String) {
        val user = auth.currentUser
        user?.let {
            updateProfileInFirestore(it.uid, "bio", bio)
        }
    }

    private fun updateProfileInFirestore(userId: String, field: String, value: String) {
        val data = hashMapOf<String, Any>(field to value) // Explicit type to resolve type mismatch
        db.collection("Profile").document(userId)
            .update(data) // Use explicit cast to Map<String, Any> for Firestore compatibility
            .addOnSuccessListener {
                if (field == "bio") {
                    profileBio.text = value
                    Toast.makeText(this, "Bio updated successfully", Toast.LENGTH_SHORT).show()
                } else if (field == "username") {
                    Toast.makeText(this, "Name updated in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error updating $field in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImage(imageType: String) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        when (imageType) {
            "profilePicture" -> pickProfileImage.launch(intent)
            "coverPhoto" -> pickCoverImage.launch(intent)
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
                        if (imageType == "profilePicture") {
                            updateProfilePhoto(uri)
                        } else if (imageType == "coverPhoto") {
                            updateCoverPhoto(uri)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfilePhoto(uri: Uri) {
        val user = auth.currentUser
        user?.let { currentUser ->
            val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(uri).build()
            currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    Glide.with(this@ProfileActivity)
                        .load(uri)
                        .into(profilePicture)
                    Toast.makeText(this@ProfileActivity, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                    updateProfileInFirestore(currentUser.uid!!, "profilePic", uri.toString()) // Ensure currentUser.uid is non-null
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@ProfileActivity, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateCoverPhoto(uri: Uri) {
        val userId = auth.currentUser?.uid
        userId?.let {
            updateProfileInFirestore(it, "coverPhoto", uri.toString())
            Glide.with(this)
                .load(uri)
                .into(coverPhoto)
            Toast.makeText(this, "Cover photo updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
