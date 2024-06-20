package com.appdev.eudemonia

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null
)

