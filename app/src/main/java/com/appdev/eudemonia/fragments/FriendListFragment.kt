package com.appdev.eudemonia.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.FriendListAdapter
import com.appdev.eudemonia.dataclasses.FriendList
import com.appdev.eudemonia.databinding.FragmentFriendListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.chat.ChatActivity

class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendListAdapter
    private val friendsList = ArrayList<FriendList>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        setupSearchButton()
        setupChatButton()
        fetchFriends()
    }

    private fun initializeRecyclerView() {
        binding.idFriendsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FriendListAdapter(friendsList)
        binding.idFriendsRecyclerView.adapter = adapter
    }

    private fun setupSearchButton() {
        val searchButton: Button = requireView().findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val intent = Intent(requireContext(), FriendsFragment::class.java)
            startActivity(intent)
        }
    }

    private fun setupChatButton() {
        val chatButton: Button = requireView().findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchFriends() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            fetchFriendsAddedByUser(db, uid)
            fetchFriendsAddedToUser(db, uid)
        } ?: run {
            // Handle the case where the user is not logged in
        }
    }

    private fun fetchFriendsAddedByUser(db: FirebaseFirestore, currentUserUid: String) {
        db.collection("Friends")
            .whereEqualTo("addedBy", currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                handleFriendsSnapshot(snapshot, "userId")
            }
            .addOnFailureListener { exception ->
                Log.e("FriendListFragment", "Error fetching friends (addedBy): ", exception)
            }
    }

    private fun fetchFriendsAddedToUser(db: FirebaseFirestore, currentUserUid: String) {
        db.collection("Friends")
            .whereEqualTo("userId", currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                handleFriendsSnapshot(snapshot, "addedBy")
            }
            .addOnFailureListener { exception ->
                Log.e("FriendListFragment", "Error fetching friends (userId): ", exception)
            }
    }

    private fun handleFriendsSnapshot(snapshot: QuerySnapshot, field: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        for (document in snapshot.documents) {
            val userId = document.getString("userId") ?: ""
            val addedBy = document.getString("addedBy") ?: ""

            if ((field == "userId" && currentUserId == addedBy) || (field == "addedBy" && currentUserId == userId)) {
                val friendUserId = if (field == "userId") userId else addedBy
                val username = if (field == "userId") document.getString("username") ?: "" else document.getString("usernameRequester") ?: ""

                fetchProfilePictureUrl(friendUserId, username)
            }
        }
    }

    private fun fetchProfilePictureUrl(friendUserId: String, username: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val profilePictureRef = storageReference.child("profile_pictures/$friendUserId")

        profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
            val profilePictureUrl = uri.toString()
            if (username.isNotEmpty()) {
                friendsList.add(FriendList(friendUserId, username, profilePictureUrl))
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { exception ->
            if (username.isNotEmpty()) {
                friendsList.add(FriendList(friendUserId, username, ""))
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
