package com.appdev.eudemonia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<Friend> {

    public FriendsAdapter(Context context, List<Friend> friends) {
        super(context, 0, friends);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_friends_list, parent, false);
        }

        ImageView profileImage = convertView.findViewById(R.id.profile_image);
        TextView friendName = convertView.findViewById(R.id.friend_name);

        // Set placeholder image and name, replace with actual data from friend object
        profileImage.setImageResource(friend.getProfileImageResId());
        friendName.setText(friend.getName());

        return convertView;
    }
}
