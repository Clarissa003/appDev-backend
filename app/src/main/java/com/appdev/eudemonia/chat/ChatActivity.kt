package com.appdev.eudemonia.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.services.DeleteOldMessagesService
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.ChatAdapter
import com.appdev.eudemonia.dataclasses.Message
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatMessages: MutableList<Message>
    private lateinit var chatWindow: RecyclerView
    private lateinit var sendButton: Button
    private lateinit var chatInput: EditText
    private lateinit var userNameTextView: TextView

    private var friendUserId: String? = null
    private var friendUsername: String? = null
    private var currentUserUsername: String? = null
    private var friendProfilePictureUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        startDeleteOldMessagesService()

        friendUserId = intent.getStringExtra("friendUserId")
        friendUsername = intent.getStringExtra("friendUsername")
        friendProfilePictureUrl = intent.getStringExtra("friendProfilePictureUrl")

        userNameTextView = findViewById(R.id.user_name)
        userNameTextView.text = friendUsername

        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        if (friendProfilePictureUrl != null) {
            Glide.with(this)
                .load(friendProfilePictureUrl)
                .placeholder(R.drawable.default_profile_picture) // Optional placeholder image
                .into(profileImageView)
        }

        db = FirebaseFirestore.getInstance()

        chatMessages = mutableListOf()
        chatAdapter = ChatAdapter(chatMessages)

        chatWindow = findViewById(R.id.chat_window)
        sendButton = findViewById(R.id.send_button)
        chatInput = findViewById(R.id.chat_input)

        chatWindow.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        sendButton.setOnClickListener {
            sendMessage()
        }

        chatInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sendButton.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchCurrentUserUsername()
    }

    private fun fetchCurrentUserUsername() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            db.collection("User").document(currentUserUid).get().addOnSuccessListener { document ->
                if (document != null) {
                    currentUserUsername = document.getString("username")
                    // Start listening for messages only after fetching username
                    listenForMessages()
                } else {
                    Log.e("ChatActivity", "No such document for current user")
                }
            }.addOnFailureListener { e ->
                Log.e("ChatActivity", "Error fetching current user data", e)
            }
        } else {
            Log.e("ChatActivity", "Current user UID is null")
        }
    }

    private fun sendMessage() {
        val messageText = chatInput.text.toString().trim()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (messageText.isNotEmpty() && friendUserId != null && currentUserUid != null && currentUserUsername != null) {
            val message = Message(
                senderId = currentUserUid,
                senderName = currentUserUsername!!,  // Ensure currentUserUsername is not null here
                receiverId = friendUserId!!,
                content = messageText,
                timestamp = Timestamp.now()
            )

            Log.d("ChatActivity", "Attempting to send message: $message")

            db.collection("Message").add(message)
                .addOnSuccessListener {
                    chatInput.text.clear()
                    Log.d("ChatActivity", "Message sent successfully")
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Log.e("ChatActivity", "Error sending message", e)
                }
        } else {
            Log.e("ChatActivity", "Message text is empty or user IDs are null")
        }
    }

    private fun listenForMessages() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null && friendUserId != null) {
            Log.d("ChatActivity", "Listening for messages between $currentUserUid and $friendUserId")

            val currentUserMessages = mutableListOf<Message>()
            val friendUserMessages = mutableListOf<Message>()

            db.collection("Message")
                .whereEqualTo("senderId", currentUserUid)
                .whereEqualTo("receiverId", friendUserId!!)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatActivity", "Error fetching messages sent by the current user", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        Log.d("ChatActivity", "Snapshot size for messages sent by current user: ${snapshot.size()}")
                        currentUserMessages.clear()
                        currentUserMessages.addAll(snapshot.documents.mapNotNull { it.toObject(
                            Message::class.java) })
                        mergeAndDisplayMessages(currentUserMessages, friendUserMessages)
                    } else {
                        Log.d("ChatActivity", "Snapshot is null for messages sent by current user")
                    }
                }

            db.collection("Message")
                .whereEqualTo("senderId", friendUserId!!)
                .whereEqualTo("receiverId", currentUserUid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatActivity", "Error fetching messages sent by the friend", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        Log.d("ChatActivity", "Snapshot size for messages sent by friend: ${snapshot.size()}")
                        friendUserMessages.clear()
                        friendUserMessages.addAll(snapshot.documents.mapNotNull { it.toObject(
                            Message::class.java) })
                        mergeAndDisplayMessages(currentUserMessages, friendUserMessages)
                    } else {
                        Log.d("ChatActivity", "Snapshot is null for messages sent by friend")
                    }
                }
        } else {
            Log.e("ChatActivity", "User IDs are null")
        }
    }

    private fun mergeAndDisplayMessages(currentUserMessages: List<Message>, friendUserMessages: List<Message>) {
        val allMessages = mutableListOf<Message>()
        allMessages.addAll(currentUserMessages)
        allMessages.addAll(friendUserMessages)
        allMessages.sortBy { it.timestamp }

        Log.d("ChatActivity", "Updating chat messages with ${allMessages.size} messages in total")
        chatAdapter.updateMessages(allMessages)
    }

    private fun startDeleteOldMessagesService() {
        val intent = Intent(this, DeleteOldMessagesService::class.java)
        startService(intent)
    }
}