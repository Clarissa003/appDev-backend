package com.appdev.eudemonia

import android.net.Uri
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Handle dynamic link
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val userId = deepLink.getQueryParameter("uid")
                    userId?.let { addFriend(it) }
                }
            }
            .addOnFailureListener {
                // Handle the error
                Toast.makeText(this, "Failed to retrieve dynamic link", Toast.LENGTH_SHORT).show()
            }

        // Placeholder data for friends list
        val listView = findViewById<ListView>(R.id.idFriends)
        val friends = mutableListOf<Friend>()
        val adapter = FriendsAdapter(this, friends)
        listView.adapter = adapter

        loadFriends(adapter, friends)
    }

    private fun addFriend(friendUid: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val friendsRef = db.collection("users").document(user.uid).collection("friends")

            val friend = hashMapOf(
                "uid" to friendUid
            )

            friendsRef.document(friendUid).set(friend)
                .addOnSuccessListener {
                    Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show()
                    // Optionally update your friends list UI here
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun loadFriends(adapter: FriendsAdapter, friends: MutableList<Friend>) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val friendsRef = db.collection("users").document(user.uid).collection("friends")

            friendsRef.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val friendUid = document.getString("uid")
                        friendUid?.let { uid ->
                            // Assuming you have a method to get friend details by UID
                            getFriendDetails(uid) { friend ->
                                friends.add(friend)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load friends: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun getFriendDetails(uid: String, callback: (Friend) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "Unknown"
                val profilePicUrl = document.getString("profilePicUrl")
                val friend = Friend(name, profilePicUrl)
                callback(friend)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to get friend details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
