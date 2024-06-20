package com.appdev.eudemonia

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FriendAdapter(private val friends: List<Friend>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)

        fun bind(friend: Friend) {
            usernameTextView.text = friend.username

            if (friend.profilePictureUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(friend.profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_picture)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("friendUserId", friend.userId)
                    putExtra("friendUsername", friend.username)
                }
                context.startActivity(intent)
            }
        }
    }
}
