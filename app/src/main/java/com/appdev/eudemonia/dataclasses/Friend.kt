package com.appdev.eudemonia.dataclasses

data class Friend(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = ""
)