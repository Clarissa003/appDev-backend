package com.appdev.eudemonia.dataclasses

data class FriendList(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = ""
)