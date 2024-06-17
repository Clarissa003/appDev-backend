package com.appdev.eudemonia

import android.os.Bundle
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchView: SearchView
    private val users = mutableListOf<User>()
    private val displayedUsers = mutableListOf<User>() // To keep track of displayed users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        listView = findViewById(R.id.idFriends)
        searchView = findViewById(R.id.idSearch)

        // Initialize adapter with the displayedUsers list
        adapter = FriendsAdapter(this, displayedUsers)
        listView.adapter = adapter

        // Load users from Firestore
        loadUsers()

        // Set up SearchView listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })
    }

    private fun loadUsers() {
        db.collection("Profile")
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
                // We don't notify the adapter here because we only want to display users when searched
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterUsers(query: String?) {
        displayedUsers.clear()
        if (!query.isNullOrEmpty()) {
            val filteredUsers = users.filter { it.username.contains(query, ignoreCase = true) }
            displayedUsers.addAll(filteredUsers)
        }
        adapter.notifyDataSetChanged()
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
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
