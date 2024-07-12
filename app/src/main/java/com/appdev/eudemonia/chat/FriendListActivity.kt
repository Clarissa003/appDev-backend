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

        initializeRecyclerView()
        fetchFriends()
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.idFriendsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendListAdapter(friendsList)
        recyclerView.adapter = adapter
    }

    private fun fetchFriends() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            fetchFriendsAddedByUser(db, uid)
            fetchFriendsAddedToUser(db, uid)
        } ?: run {
        }
    }

    private fun fetchFriendsAddedByUser(db: FirebaseFirestore, currentUserUid: String) {
        db.collection("Friends")
            .whereEqualTo("addedBy", currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                handleFriendsSnapshot(snapshot, "userId")
            }
            .addOnFailureListener { exception ->
                Log.e("FriendListActivity", "Error fetching friends (addedBy): ", exception)
            }
    }

    private fun fetchFriendsAddedToUser(db: FirebaseFirestore, currentUserUid: String) {
        db.collection("Friends")
            .whereEqualTo("userId", currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                handleFriendsSnapshot(snapshot, "addedBy")
            }
            .addOnFailureListener { exception ->
                Log.e("FriendListActivity", "Error fetching friends (userId): ", exception)
            }
    }

    private fun handleFriendsSnapshot(snapshot: QuerySnapshot, field: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        for (document in snapshot.documents) {
            val userId = document.getString("userId") ?: ""
            val addedBy = document.getString("addedBy") ?: ""

            if ((field == "userId" && currentUserId == addedBy) || (field == "addedBy" && currentUserId == userId)) {
                val friendUserId = if (field == "userId") userId else addedBy
                val username = if (field == "userId") document.getString("username") ?: "" else document.getString("usernameRequester") ?: ""

                fetchProfilePictureUrl(friendUserId, username)
            }
        }
    }

    private fun fetchProfilePictureUrl(friendUserId: String, username: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val profilePictureRef = storageReference.child("profile_pictures/$friendUserId")

        profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
            val profilePictureUrl = uri.toString()
            if (username.isNotEmpty()) {
                friendsList.add(FriendList(friendUserId, username, profilePictureUrl))
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { exception ->
            if (username.isNotEmpty()) {
                friendsList.add(FriendList(friendUserId, username, ""))
                adapter.notifyDataSetChanged()
            }
        }
    }
}
