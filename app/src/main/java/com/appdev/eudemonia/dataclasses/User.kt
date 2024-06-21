package com.appdev.eudemonia.dataclasses

data class User(
    val userId: String,
    val username: String,
    val profilePicUrl: String?,
    var isFriend: Boolean = false
)

