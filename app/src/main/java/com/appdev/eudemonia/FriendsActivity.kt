package com.appdev.eudemonia

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchView: SearchView
    private val users = mutableListOf<User>()
    private val displayedUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        listView = findViewById(R.id.idFriends)
        searchView = findViewById(R.id.idSearch)

        adapter = FriendsAdapter(this, displayedUsers)
        listView.adapter = adapter

        loadUsers()
        loadFriends()

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
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            db.collection("Profile")
                .get()
                .addOnSuccessListener { documents ->
                    users.clear()
                    displayedUsers.clear()
                    for (document in documents) {
                        val userId = document.id
                        val username = document.getString("username") ?: "Unknown"
                        val profilePicUrl = document.getString("profilePicUrl")
                        val user = User(userId, username, profilePicUrl)

                        checkIfFriend(currentUser.uid, userId) { isFriend ->
                            user.isFriend = isFriend
                            users.add(user)
                            if (isFriend) {
                                displayedUsers.add(user)
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkIfFriend(currentUserId: String, friendUserId: String, callback: (Boolean) -> Unit) {
        db.collection("Friends").whereEqualTo("userId", friendUserId).whereEqualTo("addedBy", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty.not())
            }
            .addOnFailureListener { e ->
                Log.e("FriendsActivity", "Error checking if user is friend", e)
                callback(false)
            }
    }

    private fun loadFriends() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("Friends").whereEqualTo("addedBy", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val userId = document.getString("userId")
                        userId?.let { friendId ->
                            users.find { it.userId == friendId }?.let { friend ->
                                friend.isFriend = true
                                displayedUsers.add(friend)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load friends: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun filterUsers(query: String?) {
        displayedUsers.clear()
        if (!query.isNullOrEmpty()) {
            val filteredUsers = users.filter { it.username.contains(query, ignoreCase = true) }
            displayedUsers.addAll(filteredUsers)
        } else {
            displayedUsers.addAll(users.filter { it.isFriend })
        }
        adapter.notifyDataSetChanged()
    }

    fun addFriend(user: User) {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            val friendsRef = db.collection("Friends")

            val friendData = hashMapOf(
                "userId" to user.userId,
                "username" to user.username,
                "profilePicUrl" to user.profilePicUrl,
                "addedBy" to currentUser.uid
            )

            friendsRef.add(friendData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Added ${user.username} as friend!", Toast.LENGTH_SHORT).show()

                    user.isFriend = true
                    if (!displayedUsers.contains(user)) {
                        displayedUsers.add(user)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    fun removeFriend(user: User) {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            db.collection("Friends")
                .whereEqualTo("userId", user.userId)
                .whereEqualTo("addedBy", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("Friends").document(document.id).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Removed ${user.username} as friend!", Toast.LENGTH_SHORT).show()

                                user.isFriend = false
                                displayedUsers.remove(user)
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to remove friend: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to find friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
