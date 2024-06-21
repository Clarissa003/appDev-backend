package com.appdev.eudemonia.chat

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.FriendListAdapter
import com.appdev.eudemonia.dataclasses.FriendList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class FriendListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendListAdapter
    private val friendsList = ArrayList<FriendList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        Log.d("FriendsActivity", "onCreate called")

        recyclerView = findViewById(R.id.idFriendsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FriendListAdapter(friendsList)
        recyclerView.adapter = adapter

        fetchFriends()
    }

    private fun fetchFriends() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            Log.d("FriendsActivity", "Current user UID: $currentUserUid")

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

            val friendUserId: String
            val username: String

            if (field == "userId" && currentUserId == addedBy) {
                friendUserId = userId
                username = document.getString("username") ?: ""
            } else if (field == "addedBy" && currentUserId == userId) {
                friendUserId = addedBy
                username = document.getString("usernameRequester") ?: ""
            } else {
                continue
            }

            val storageReference = FirebaseStorage.getInstance().reference
            val profilePictureRef = storageReference.child("profile_pictures/${friendUserId}") // Adjust path as per your storage structure

            profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
                val profilePictureUrl = uri.toString()

                if (username.isNotEmpty()) {
                    friendsList.add(FriendList(friendUserId, username, profilePictureUrl))
                    adapter.notifyDataSetChanged() // Notify adapter after adding data
                }
            }.addOnFailureListener { exception ->
                Log.e("FriendsActivity", "Error fetching profile picture URL for $friendUserId", exception)
                if (username.isNotEmpty()) {
                    friendsList.add(FriendList(friendUserId, username, ""))
                    adapter.notifyDataSetChanged() // Notify adapter after adding data
                }
            }
        }
    }
}
