package com.appdev.eudemonia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
                    .placeholder(R.drawable.default_profile_picture) // Placeholder if image loading fails
                    .error(R.drawable.default_profile_picture) // Error image if Glide fails to load image
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.default_profile_picture) // Default image if profilePicUrl is null or blank
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
