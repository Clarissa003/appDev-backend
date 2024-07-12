package com.appdev.eudemonia.friends

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

    private lateinit var senderId: String
    private lateinit var usernameTextView: TextView
    private lateinit var addFriendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request_detail)

        initFirebase()
        initViews()
        loadSenderDetails()
        setupAddFriendButton()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initViews() {
        senderId = intent.getStringExtra("senderId") ?: ""
        usernameTextView = findViewById(R.id.usernameTextView)
        addFriendButton = findViewById(R.id.addFriendButton)
    }

    private fun loadSenderDetails() {
        db.collection("Profile").document(senderId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                usernameTextView.text = username
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching sender details", e)
            }
    }

    private fun setupAddFriendButton() {
        addFriendButton.setOnClickListener {
            addFriend()
        }
    }

    private fun addFriend() {
        val username = usernameTextView.text.toString()
        val profilePicUrl = intent.getStringExtra("profilePicUrl")

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { currentUser ->
            val friendsRef = db.collection("Friends")

            val friendData = createFriendData(senderId, username, profilePicUrl, currentUser.uid)
            friendsRef.add(friendData)
                .addOnSuccessListener {
                    handleAddFriendSuccess(username)
                }
                .addOnFailureListener { e ->
                    handleAddFriendFailure(e)
                }

            val requesterData = createRequesterData(currentUser.uid, currentUser.displayName, currentUser.photoUrl.toString(), currentUser.uid)
            friendsRef.add(requesterData)
                .addOnSuccessListener {
                    Log.d(TAG, "Added requester as friend for the user")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding requester as friend for the user", e)
                }
        }
    }

    private fun createFriendData(userId: String, username: String, profilePicUrl: String?, addedBy: String): HashMap<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "username" to username,
            "profilePicUrl" to (profilePicUrl ?: ""),
            "addedBy" to addedBy
        )
    }

    private fun createRequesterData(userId: String, username: String?, profilePicUrl: String?, addedBy: String): HashMap<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "username" to (username ?: ""),
            "profilePicUrl" to (profilePicUrl ?: ""),
            "addedBy" to addedBy
        )
    }

    private fun handleAddFriendSuccess(username: String) {
        Toast.makeText(this, "Added $username as friend!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FriendListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleAddFriendFailure(exception: Exception) {
        Toast.makeText(this, "Failed to add friend: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "FriendRequestDetailActivity"
    }
}
