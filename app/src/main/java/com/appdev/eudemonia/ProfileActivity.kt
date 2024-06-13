package com.appdev.eudemonia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileDisplayData: TextView
    private lateinit var shareButton: ImageButton

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePicture)
        profileName = findViewById(R.id.profileName)
        profileDisplayData = findViewById(R.id.profileBio)
        shareButton = findViewById(R.id.shareButton)

        loadUserProfile()

        profilePicture.setOnClickListener {
            selectImage()
        }
        shareButton.setOnClickListener {
            generateFriendLink()
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
    private fun generateFriendLink() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.yourapp.com/addfriend?uid=$uid"))
                .setDomainUriPrefix("https://yourapp.page.link")
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder(packageName)
                        .build()
                )
                .buildDynamicLink()

            val dynamicLinkUri = dynamicLink.uri
            copyLinkToClipboard(dynamicLinkUri.toString())
        }
    }

    private fun copyLinkToClipboard(link: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Friend Link", link)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
    }


}
