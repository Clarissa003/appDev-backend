package com.appdev.eudemonia;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<Friend> {

    public FriendsAdapter(Context context, List<Friend> friends) {
        super(context, 0, friends);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Friend friend = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_friends_list, parent, false);
        }

        ImageView profileImage = convertView.findViewById(R.id.profile_image);
        TextView friendName = convertView.findViewById(R.id.friend_name);

        Log.d(TAG, "getView: " + friend.getName() + ", " + friend.getProfileImageResId());

        if (friend != null) {
            profileImage.setImageResource(friend.getProfileImageResId());
            friendName.setText(friend.getName());
        }

        return convertView;
    }
}
