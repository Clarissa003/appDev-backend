package com.appdev.eudemonia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.databinding.ActivitySongsListBinding
import com.appdev.eudemonia.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class SongsListActivity : AppCompatActivity() {

    lateinit var binding: ActivitySongsListBinding
    lateinit var songsListAdapter: SongsListAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Fetch songs from Firestore
        fetchSongsFromFirestore()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSongsListRecyclerView()
    }

    private fun fetchSongsFromFirestore() {
        firestore.collection("songs").get()
            .addOnSuccessListener { result ->
                val songsList = mutableListOf<SongModel>()
                for (document in result) {
                    val song = document.toObject(SongModel::class.java)
                    songsList.add(song)
                }
                if (songsList.isNotEmpty()) {
                    // Set the activity header with the first song's title
                    binding.nameTextView.text = songsList[0].title
                } else {
                    Log.e("SongsListActivity", "No songs found!")
                }
                // Update the adapter with the fetched songs
                songsListAdapter.updateSongsList(songsList)
            }
            .addOnFailureListener { exception ->
                Log.e("SongsListActivity", "Error getting documents: ", exception)
            }
    }

    private fun setupSongsListRecyclerView() {
        songsListAdapter = SongsListAdapter(listOf())
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }
}
