package com.appdev.eudemonia.friends

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.chat.FriendListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequestDetailActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request_detail)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val senderId = intent.getStringExtra("senderId")
        senderId?.let { loadSenderDetails(it) } // Load sender details if senderId is not null

        val addFriendButton: Button = findViewById(R.id.addFriendButton)
        addFriendButton.setOnClickListener {
            senderId?.let { id ->
                // Extracted username and profilePicUrl from TextView and ImageView
                val username = findViewById<TextView>(R.id.usernameTextView).text.toString()
                val profilePicUrl = intent.getStringExtra("profilePicUrl")
                addFriend(id, username, profilePicUrl)
            }
        }
    }

    private fun loadSenderDetails(senderId: String) {
        db.collection("Profile").document(senderId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                val profilePicUrl = document.getString("profilePicUrl")

                findViewById<TextView>(R.id.usernameTextView).text = username
               /* profilePicUrl?.let {
                    val imageView = findViewById<ImageView>(R.id.profileImageView)
                    Picasso.get().load(it).into(imageView)
                }*/
            }
            .addOnFailureListener { e ->
                Log.e("FriendRequestDetail", "Error fetching sender details", e)
            }
    }

    private fun addFriend(userId: String, username: String, profilePicUrl: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { currentUser ->
            val friendsRef = db.collection("Friends")

            val friendData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "profilePicUrl" to (profilePicUrl ?: ""), // Ensure profilePicUrl is not null
                "addedBy" to currentUser.uid
            )

            friendsRef.add(friendData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Added $username as friend!", Toast.LENGTH_SHORT).show()
                    // Redirect to FriendsActivity after adding friend
                    val intent = Intent(this, FriendListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            // Prepare data for requester's friend list
            val friendDataRequester = hashMapOf(
                "userId" to currentUser.uid,
                "username" to currentUser.displayName, // Use current user's display name
                "profilePicUrl" to currentUser.photoUrl.toString(), // Use current user's profile pic URL
                "addedBy" to currentUser.uid
            )

            // Add requester as friend to the user's friend list
            FirebaseFirestore.getInstance().collection("Friends")
                .add(friendDataRequester)
                .addOnSuccessListener {
                    Log.d(TAG, "Added requester as friend for the user")
                    // Optionally, you can handle success if needed
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding requester as friend for the user", e)
                    // Handle failure if needed
                }
        }
    }
}

