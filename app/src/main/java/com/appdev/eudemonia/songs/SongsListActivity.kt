package com.appdev.eudemonia.songs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.databinding.ActivitySongsListBinding
import com.appdev.eudemonia.adapters.SongsListAdapter
import com.appdev.eudemonia.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongsListFragment : Fragment() {

    private lateinit var binding: ActivitySongsListBinding
    private lateinit var songsListAdapter: SongsListAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivitySongsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupEdgeToEdge()
        setupSongsListRecyclerView()
        fetchSongsFromFirestore()
    }

    private fun setupUI() {
        // Any additional UI setup can go here
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchSongsFromFirestore() {
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("songs").get()
            .addOnSuccessListener { result ->
                val songsList = mutableListOf<SongModel>()
                for (document in result) {
                    val song = document.toObject(SongModel::class.java)
                    songsList.add(song)
                }
                Log.d("SongsListFragment", "Fetched ${songsList.size} songs")
                songsListAdapter.updateSongsList(songsList)
            }
            .addOnFailureListener { exception ->
                Log.e("SongsListFragment", "Error getting documents: ", exception)
            }
    }

    private fun setupSongsListRecyclerView() {
        songsListAdapter = SongsListAdapter(emptyList())
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }
}
