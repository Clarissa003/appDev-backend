package com.appdev.eudemonia

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var adapter: FriendsAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        listView = findViewById(R.id.idFriends)
        adapter = FriendsAdapter(this, users)
        listView.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        db.collection("Profile") // Adjust this to match your Firestore collection name
            .get()
            .addOnSuccessListener { documents ->
                users.clear()
                for (document in documents) {
                    val userId = document.id
                    val username = document.getString("username") ?: "Unknown"
                    val profilePicUrl = document.getString("profilePicUrl")
                    val user = User(userId, username, profilePicUrl)
                    users.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun addFriend(user: User) {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            val friendsRef = db.collection("Friends").document(currentUser.uid)

            val friendData = hashMapOf(
                "userId" to user.userId,
                "username" to user.username,
                "profilePicUrl" to user.profilePicUrl
            )

            friendsRef.collection("userFriends").document(user.userId).set(friendData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Added ${user.username} as friend!", Toast.LENGTH_SHORT).show()
                    // Optionally update UI or handle further actions
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}