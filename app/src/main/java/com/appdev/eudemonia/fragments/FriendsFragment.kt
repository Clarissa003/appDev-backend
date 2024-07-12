package com.appdev.eudemonia.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.FriendsAdapter
import com.appdev.eudemonia.databinding.FragmentFriendsBinding
import com.appdev.eudemonia.dataclasses.User
import com.appdev.eudemonia.friends.FriendRequestDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FriendsAdapter
    private val users = mutableListOf<User>()
    private val displayedUsers = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebase()
        setupRecyclerView()
        setupSearchView()

        loadUsers()
        loadFriends()
        listenForFriendRequests()

        // Setup navigation for the Friend Requests button
        binding.buttonFriendRequests.setOnClickListener {
            findNavController().navigate(R.id.action_friends_to_friend_requests)
        }
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(requireContext(), displayedUsers, ::addFriend, ::removeFriend)
        binding.idFriendsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.idFriendsRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.idSearch.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
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
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createNotificationChannel()

            val intent = Intent(requireContext(), FriendRequestDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("senderId", senderId)
            }
            val pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.VIBRATE),
                PERMISSION_REQUEST_CODE
            )
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
                    Toast.makeText(
                        requireContext(),
                        "Failed to load users: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun checkIfFriend(
        currentUserId: String,
        friendUserId: String,
        callback: (Boolean) -> Unit
    ) {
        db.collection("Friends").whereEqualTo("userId", friendUserId)
            .whereEqualTo("addedBy", currentUserId)
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
                    Toast.makeText(
                        requireContext(),
                        "Failed to load friends: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    Toast.makeText(
                        requireContext(),
                        "Friend request sent to ${user.username}!",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    Toast.makeText(
                        requireContext(),
                        "Failed to send friend request: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
                                Toast.makeText(
                                    requireContext(),
                                    "Removed ${user.username} as friend!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                user.isFriend = false
                                displayedUsers.remove(user)
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to remove friend: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to find friend: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission denied. Cannot show notifications.",
                        Toast.LENGTH_SHORT
                    ).show()
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
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "FriendsFragment"
        private const val CHANNEL_ID = "friend_requests_channel"
        private const val NOTIFICATION_ID = 1
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
