package com.appdev.eudemonia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.dataclasses.FriendList
import com.bumptech.glide.Glide

class FriendsAdapter(private val friendsList: List<FriendList>) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.usernameTextView.text = friend.username
        Glide.with(holder.profileImageView.context)
            .load(friend.profilePictureUrl)
            .into(holder.profileImageView)
    }

    override fun getItemCount() = friendsList.size

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    }
}
