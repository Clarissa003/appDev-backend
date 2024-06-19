package com.appdev.eudemonia

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatWindow: RecyclerView
    private lateinit var sendButton: Button
    private lateinit var chatInput: EditText
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("chats")

        chatMessages = arrayListOf()
        chatAdapter = ChatAdapter(chatMessages)

        // Initialize views
        chatWindow = findViewById(R.id.chat_window)
        sendButton = findViewById(R.id.send_button)
        chatInput = findViewById(R.id.chat_input)
        userNameTextView = findViewById(R.id.user_name)

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

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = chatInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val messageId = database.push().key ?: ""
            val chatMessage = ChatMessage(messageId, userNameTextView.text.toString(), messageText)
            database.child(messageId).setValue(chatMessage)
            chatInput.text.clear()
        }
    }

    private fun listenForMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    chatMessages.add(chatMessage)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    chatWindow.scrollToPosition(chatMessages.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
