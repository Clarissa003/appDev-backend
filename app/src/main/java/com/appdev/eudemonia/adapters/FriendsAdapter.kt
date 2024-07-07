package com.appdev.eudemonia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.dataclasses.User
import com.bumptech.glide.Glide

class FriendsAdapter(
    private val context: Context,
    private val users: List<User>,
    private val addFriendCallback: (User) -> Unit,
    private val removeFriendCallback: (User) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = users[position]
        holder.friendName.text = user.username

        val profilePicUrl = user.profilePicUrl ?: ""

        if (profilePicUrl.isNotEmpty()) {
            Glide.with(context)
                .load(profilePicUrl)
                .placeholder(R.drawable.default_profile_picture)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile_picture)
        }

        if (user.isFriend) {
            holder.addButton.visibility = View.GONE
            holder.removeButton.visibility = View.VISIBLE
            holder.removeButton.setOnClickListener { removeFriendCallback(user) }
        } else {
            holder.addButton.visibility = View.VISIBLE
            holder.removeButton.visibility = View.GONE
            holder.addButton.setOnClickListener { addFriendCallback(user) }
        }
    }

    override fun getItemCount(): Int = users.size

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImageView)
        val friendName: TextView = view.findViewById(R.id.usernameTextView)
        val addButton: Button = view.findViewById(R.id.add_button)
        val removeButton: Button = view.findViewById(R.id.remove_button)
    }
}
