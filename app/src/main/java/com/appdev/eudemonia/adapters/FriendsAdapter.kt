package com.appdev.eudemonia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.appdev.eudemonia.friends.FriendsActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.dataclasses.User
import com.bumptech.glide.Glide

class FriendsAdapter(private val activity: FriendsActivity, users: List<User>) : ArrayAdapter<User>(activity, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(activity).inflate(R.layout.activity_add_friends, parent, false)
        val user = getItem(position)

        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val friendName = view.findViewById<TextView>(R.id.friend_name)
        val addButton = view.findViewById<Button>(R.id.add_button)
        val removeButton = view.findViewById<Button>(R.id.remove_button)

        user?.let {
            friendName.text = it.username

            // Load profile picture if available, otherwise set default image
            if (it.profilePicUrl != null && it.profilePicUrl.isNotBlank()) {
                Glide.with(activity)
                    .load(it.profilePicUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.default_profile_picture)
            }

            if (it.isFriend) {
                addButton.visibility = View.GONE
                removeButton.visibility = View.VISIBLE
                removeButton.setOnClickListener {
                    val clickedUser = getItem(position)
                    clickedUser?.let { user ->
                        activity.removeFriend(user)
                    }
                }
            } else {
                addButton.visibility = View.VISIBLE
                removeButton.visibility = View.GONE
                addButton.text = "Add"
                addButton.isEnabled = true
                addButton.setOnClickListener {
                    val clickedUser = getItem(position)
                    clickedUser?.let { user ->
                        activity.addFriend(user)
                    }
                }
            }
        }

        return view
    }
}