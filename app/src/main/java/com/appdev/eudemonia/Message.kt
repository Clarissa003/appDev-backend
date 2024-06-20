package com.appdev.eudemonia

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

