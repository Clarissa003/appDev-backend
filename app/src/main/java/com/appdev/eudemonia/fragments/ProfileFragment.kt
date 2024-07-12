package com.appdev.eudemonia.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.appdev.eudemonia.R
import com.appdev.eudemonia.authentication.LoginActivity
import com.appdev.eudemonia.settings.SettingsActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        initializeViews(rootView)

        if (auth.currentUser == null) {
            navigateToLogin()
            return rootView
        }

        setupClickListeners()

        loadUserProfile()

        return rootView
    }

    private fun initializeViews(view: View) {
        profilePicture = view.findViewById(R.id.profilePicture)
        coverPhoto = view.findViewById(R.id.coverPhoto)
        profileName = view.findViewById(R.id.profileName)
        profileBio = view.findViewById(R.id.profileBio)
        editProfileNameButton = view.findViewById(R.id.editProfileNameButton)
        editBioButton = view.findViewById(R.id.editBio)
        settingsButton = view.findViewById(R.id.settingsButton)
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun setupClickListeners() {
        profilePicture.setOnClickListener { selectImage("profilePicture") }
        coverPhoto.setOnClickListener { selectImage("coverPhoto") }
        editProfileNameButton.setOnClickListener { showEditDialog("name") }
        editBioButton.setOnClickListener { showEditDialog("bio") }
        settingsButton.setOnClickListener { navigateToSettings() }
    }

    private fun navigateToSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
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
                    Toast.makeText(activity, "Error getting profile data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showEditDialog(field: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit $field")

        val input = EditText(requireContext())
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
                    Toast.makeText(activity, "Name updated successfully", Toast.LENGTH_SHORT).show()
                    updateProfileInFirestore(currentUser.uid, "username", name)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Failed to update name: ${exception.message}", Toast.LENGTH_SHORT).show()
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
        val data = hashMapOf<String, Any>(field to value)
        db.collection("Profile").document(userId)
            .update(data)
            .addOnSuccessListener {
                if (field == "bio") {
                    profileBio.text = value
                    Toast.makeText(activity, "Bio updated successfully", Toast.LENGTH_SHORT).show()
                } else if (field == "username") {
                    Toast.makeText(activity, "Name updated in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Error updating $field in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    uploadImage(uri, "profilePicture")
                }
            }
        }

    private val pickCoverImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
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
                    Toast.makeText(activity, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfilePhoto(uri: Uri) {
        val user = auth.currentUser
        user?.let { currentUser ->
            val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(uri).build()
            currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    Glide.with(this)
                        .load(uri)
                        .into(profilePicture)
                    Toast.makeText(activity, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                    updateProfileInFirestore(currentUser.uid!!, "profilePic", uri.toString())
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(activity, "Cover photo updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
