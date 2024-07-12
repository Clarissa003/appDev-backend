package com.appdev.eudemonia.dataclasses

data class FriendList(
    val userId: String,
    val username: String,
    val profilePictureUrl: String = ""
)

object FriendListData {
    fun getFriends(): List<FriendList> {
        return listOf(
            FriendList("1", "John Doe", "https://example.com/johndoe.jpg"),
            FriendList("2", "Jane Smith", "https://example.com/janesmith.jpg")
        )
    }
}
