package com.appdev.eudemonia.friends

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.FriendsAdapter
import com.appdev.eudemonia.base.BaseActivity
import com.appdev.eudemonia.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchView: SearchView
    private val users = mutableListOf<User>()
    private val displayedUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        initFirebase()
        initViews()
        setupRecyclerView()
        setupSearchView()

        loadUsers()
        loadFriends()
        listenForFriendRequests()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.idFriendsRecyclerView)
        searchView = findViewById(R.id.idSearch)
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(this, displayedUsers, ::addFriend, ::removeFriend)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
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

    private fun listenForFriendRequests() {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            db.collection("FriendRequests")
                .whereEqualTo("receiverId", currentUser.uid)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    for (dc in snapshots!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val senderId = dc.document.getString("senderId")
                                senderId?.let { fetchSenderDetailsAndNotify(it) }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                // No action needed for modified case
                            }
                            DocumentChange.Type.REMOVED -> {
                                // No action needed for removed case
                            }
                        }
                    }
                }
        }
    }

    private fun fetchSenderDetailsAndNotify(senderId: String) {
        db.collection("Profile").document(senderId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                sendFriendRequestNotification(username, senderId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching sender details", e)
            }
    }

    private fun sendFriendRequestNotification(username: String, senderId: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            createNotificationChannel()

            val intent = Intent(this, FriendRequestDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("senderId", senderId)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Friend Request")
                .setContentText("$username wants to add you as a friend.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.VIBRATE), PERMISSION_REQUEST_CODE)
        }
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
                        val user = User(userId, username, profilePicUrl ?: "")

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
                callback(!documents.isEmpty)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking if user is friend", e)
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
            val friendRequestsRef = db.collection("FriendRequests")

            val friendRequestData = hashMapOf(
                "senderId" to currentUser.uid,
                "receiverId" to user.userId,
                "timestamp" to System.currentTimeMillis()
            )

            friendRequestsRef.add(friendRequestData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Friend request sent to ${user.username}!", Toast.LENGTH_SHORT).show()
                    // Fetch usernames
                    val usernameReceiver = user.username ?: "Unknown"
                    val usernameRequester = currentUser.displayName ?: "Unknown"
                    // Update requester's friend list as well
                    val friendsRef = db.collection("Friends")

                    val friendData = hashMapOf(
                        "userId" to user.userId,
                        "username" to usernameReceiver,
                        "profilePicUrl" to (user.profilePicUrl ?: ""),
                        "usernameRequester" to usernameRequester,
                        "addedBy" to currentUser.uid
                    )

                    friendsRef.add(friendData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Added ${user.username} as friend for requester")
                            // Optionally handle success if needed
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding ${user.username} as friend for requester", e)
                            // Handle failure if needed
                        }
                    sendFriendRequestNotification(user.username, user.userId)

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send friend request: ${e.message}", Toast.LENGTH_SHORT).show()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                } else {
                    Toast.makeText(this, "Permission denied. Cannot show notifications.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Friend Requests"
            val descriptionText = "Notifications for friend requests"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "FriendsActivity"
        private const val CHANNEL_ID = "friend_requests_channel"
        private const val NOTIFICATION_ID = 1
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
