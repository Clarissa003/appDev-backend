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
        val view = convertView ?: LayoutInflater.from(activity).inflate(R.layout.activity_friends_list, parent, false)
        val user = getItem(position)

        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val friendName = view.findViewById<TextView>(R.id.friend_name)
        val addButton = view.findViewById<Button>(R.id.add_button)

        user?.let {
            friendName.text = it.username
            if (it.profilePicUrl != null) {
                Glide.with(activity).load(it.profilePicUrl).into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.default_profile_picture)
            }

            if (it.isFriend) {
                addButton.text = "Friend"
                addButton.isEnabled = false
            } else {
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
