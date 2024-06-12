package com.appdev.eudemonia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class FriendsAdapter(context: Context, friends: List<Friend>) : ArrayAdapter<Friend>(context, 0, friends) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val friend = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_friends_list, parent, false)
        }

        val profileImage = view?.findViewById<ImageView>(R.id.profile_image)
        val friendName = view?.findViewById<TextView>(R.id.friend_name)

        friend?.let {
            friendName?.text = it.name
            profileImage?.let { imageView ->
                if (it.profileImageUrl != null) {
                    Glide.with(context).load(it.profileImageUrl).into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.default_profile_picture)
                }
            }
        }

        return view!!
    }
}
