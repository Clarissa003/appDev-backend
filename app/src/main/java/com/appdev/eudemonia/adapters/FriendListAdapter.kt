package com.appdev.eudemonia.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.chat.ChatActivity
import com.appdev.eudemonia.dataclasses.FriendList
import com.appdev.eudemonia.R
import com.bumptech.glide.Glide

class FriendListAdapter(private val friendLists: List<FriendList>) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendLists[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friendLists.size
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)

        fun bind(friendList: FriendList) {
            usernameTextView.text = friendList.username

            if (friendList.profilePictureUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(friendList.profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_picture)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("friendUserId", friendList.userId)
                    putExtra("friendUsername", friendList.username)
                    putExtra("friendProfilePictureUrl", friendList.profilePictureUrl)
                }
                context.startActivity(intent)
            }
        }
    }
}
