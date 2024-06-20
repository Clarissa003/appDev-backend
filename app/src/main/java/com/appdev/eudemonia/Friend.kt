package com.appdev.eudemonia

data class Friend(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = ""
)