package com.appdev.eudemonia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.graphics.Color
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class FriendsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter
    private val friendsList = ArrayList<Friend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        Log.d("FriendsActivity", "onCreate called")

        recyclerView = findViewById(R.id.idFriendsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendAdapter(friendsList)
        recyclerView.adapter = adapter


        fetchFriends()
    }

    private fun fetchFriends() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            Log.d("FriendsActivity", "Current user UID: $currentUserUid")

            // Fetch friends added by the current user
            db.collection("Friends")
                .whereEqualTo("addedBy", currentUserUid)
                .get()
                .addOnSuccessListener { addedBySnapshot ->
                    Log.d("FriendsActivity", "AddedBy snapshot size: ${addedBySnapshot.size()}")
                    handleFriendsSnapshot(addedBySnapshot, "userId")
                }
                .addOnFailureListener { exception ->
                    Log.e("FriendsActivity", "Error fetching friends (addedBy): ", exception)
                }

            // Fetch friends where the current user is added as a friend
            db.collection("Friends")
                .whereEqualTo("userId", currentUserUid)
                .get()
                .addOnSuccessListener { userIdSnapshot ->
                    Log.d("FriendsActivity", "UserId snapshot size: ${userIdSnapshot.size()}")
                    handleFriendsSnapshot(userIdSnapshot, "addedBy")
                }
                .addOnFailureListener { exception ->
                    Log.e("FriendsActivity", "Error fetching friends (userId): ", exception)
                }
        } else {
            Log.e("FriendsActivity", "Current user UID is null")
        }
    }

    private fun handleFriendsSnapshot(snapshot: QuerySnapshot, field: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        for (document in snapshot.documents) {
            val userId = document.getString("userId") ?: ""
            val addedBy = document.getString("addedBy") ?: ""

            // Determine which ID and username to fetch based on the current user's role in the friendship
            val friendUserId: String
            val username: String

            if (field == "userId" && currentUserId == addedBy) {
                // Current user initiated the friendship, fetch username of the friend
                friendUserId = userId
                username = document.getString("username") ?: ""
            } else if (field == "addedBy" && currentUserId == userId) {
                // Current user was added by someone else, fetch username of the current user
                friendUserId = addedBy
                username = document.getString("usernameRequester") ?: ""
            } else {
                // In case of unexpected conditions, continue to the next document
                continue
            }

            // Fetch profile picture URL from Firebase Storage
            val storageReference = FirebaseStorage.getInstance().reference
            val profilePictureRef = storageReference.child("gs://eudemonia-c9eac.appspot.com/profile_pictures/${friendUserId}.jpg") // Adjust path as per your storage structure

            profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
                val profilePictureUrl = uri.toString()

                // If username is not empty, add Friend to the list
                if (username.isNotEmpty()) {
                    friendsList.add(Friend(friendUserId, username, profilePictureUrl))
                    adapter.notifyDataSetChanged() // Notify adapter after adding data
                }
            }.addOnFailureListener { exception ->
                Log.e("FriendsActivity", "Error fetching profile picture URL for $friendUserId", exception)
                // Add friend with default profile picture URL if fetching fails
                if (username.isNotEmpty()) {
                    friendsList.add(Friend(friendUserId, username, ""))
                    adapter.notifyDataSetChanged() // Notify adapter after adding data
                }
            }
        }
    }




}
