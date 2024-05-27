package com.appdev.eudemonia;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        ListView listView = findViewById(R.id.idFriends);

        // Placeholder data
        List<Friend> friends = new ArrayList<>();
        friends.add(new Friend("John Doe", R.drawable.default_profile_picture));
        friends.add(new Friend("Jane Smith", R.drawable.default_profile_picture));
        // Add more friends as needed

        FriendsAdapter adapter = new FriendsAdapter(this, friends);
        listView.setAdapter(adapter);
    }
}
