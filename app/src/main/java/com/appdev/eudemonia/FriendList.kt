package com.appdev.eudemonia

data class FriendList(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = ""
)