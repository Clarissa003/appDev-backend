package com.appdev.eudemonia.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.FriendsAdapter
import com.appdev.eudemonia.dataclasses.User
import com.appdev.eudemonia.friends.FriendRequestDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchView: SearchView
    private val users = mutableListOf<User>()
    private val displayedUsers = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        initFirebase()
        initViews(view)
        setupRecyclerView()
        setupSearchView()

        loadUsers()
        loadFriends()
        listenForFriendRequests()
        return view
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.idFriendsRecyclerView)
        searchView = view.findViewById(R.id.idSearch)
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(requireContext(), displayedUsers, ::addFriend, ::removeFriend)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            createNotificationChannel()

            val intent = Intent(requireContext(), FriendRequestDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("senderId", senderId)
            }
            val pendingIntent = PendingIntent.getActivity(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Friend Request")
                .setContentText("$username wants to add you as a friend.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(requireContext())) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.VIBRATE), PERMISSION_REQUEST_CODE)
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
                        val profilePicUrl = document.getString("profilePicUrl") ?: ""
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
                    Log.e(TAG, "Error loading users", e)
                }
        }
    }

    private fun loadFriends() {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            db.collection("Friends")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val friendId = document.getString("friendId")
                        friendId?.let { id ->
                            users.find { it.userId == id }?.isFriend = true
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading friends", e)
                }
        }
    }

    private fun checkIfFriend(currentUserId: String, userId: String, callback: (Boolean) -> Unit) {
        db.collection("Friends")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("friendId", userId)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty.not())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking if friend", e)
                callback(false)
            }
    }

    private fun filterUsers(query: String?) {
        val filteredUsers = if (query.isNullOrEmpty()) {
            users
        } else {
            users.filter {
                it.username.contains(query, ignoreCase = true)
            }
        }
        displayedUsers.clear()
        displayedUsers.addAll(filteredUsers)
        adapter.notifyDataSetChanged()
    }

    private fun addFriend(user: User) {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            val friendMap = hashMapOf(
                "userId" to currentUser.uid,
                "friendId" to user.userId
            )
            db.collection("Friends").add(friendMap)
                .addOnSuccessListener {
                    user.isFriend = true
                    adapter.notifyDataSetChanged()
                    Toast.makeText(context, "${user.username} added as friend", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding friend", e)
                    Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun removeFriend(user: User) {
        val currentUser = auth.currentUser
        currentUser?.let { currentUser ->
            db.collection("Friends")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendId", user.userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("Friends").document(document.id).delete()
                            .addOnSuccessListener {
                                user.isFriend = false
                                adapter.notifyDataSetChanged()
                                Toast.makeText(context, "${user.username} removed from friends", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error removing friend", e)
                                Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error finding friend", e)
                    Toast.makeText(context, "Failed to find friend", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val TAG = "FriendsFragment"
        private const val CHANNEL_ID = "FRIEND_REQUEST_CHANNEL"
        private const val NOTIFICATION_ID = 1
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Friend Request"
            val descriptionText = "Channel for friend request notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
