package com.appdev.eudemonia.songs

import android.os.Bundle
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.databinding.ActivitySongsListBinding
import com.appdev.eudemonia.adapters.SongsListAdapter
import com.appdev.eudemonia.base.BaseActivity
import com.appdev.eudemonia.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongsListActivity : BaseActivity() {

    private lateinit var binding: ActivitySongsListBinding
    private lateinit var songsListAdapter: SongsListAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupEdgeToEdge()
        setupSongsListRecyclerView()
        fetchSongsFromFirestore()
    }

    private fun setupUI() {
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                Log.d("SongsListActivity", "Fetched ${songsList.size} songs")
                songsListAdapter.updateSongsList(songsList)
            }
            .addOnFailureListener { exception ->
                Log.e("SongsListActivity", "Error getting documents: ", exception)
            }
    }

    private fun setupSongsListRecyclerView() {
        songsListAdapter = SongsListAdapter(mutableListOf()) { song ->
        }
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }
}
