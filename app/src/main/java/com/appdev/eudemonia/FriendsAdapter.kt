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

class FriendsAdapter(context: Context, users: List<User>) : ArrayAdapter<User>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val user = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_friends_list, parent, false)
        }

        val profileImage = view!!.findViewById<ImageView>(R.id.profile_image)
        val friendName = view.findViewById<TextView>(R.id.friend_name)
        val addButton = view.findViewById<Button>(R.id.add_button)

        user?.let {
            friendName.text = it.username
            profileImage?.let { imageView ->
                if (it.profilePicUrl != null) {
                    Glide.with(context).load(it.profilePicUrl).into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.default_profile_picture)
                }
            }

            addButton?.setOnClickListener {
                val friend = getItem(position) // Get the User object at this position
                if (friend != null && context is FriendsActivity) {
                    (context as FriendsActivity).addFriend(friend)
                }
            }
        }

        return view
    }
}
