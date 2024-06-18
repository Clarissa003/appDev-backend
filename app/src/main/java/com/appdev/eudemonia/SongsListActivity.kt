package com.appdev.eudemonia

import SongsListAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.databinding.ActivitySongsListBinding
import com.appdev.eudemonia.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongsListActivity : AppCompatActivity() {

    lateinit var binding: ActivitySongsListBinding
    lateinit var songsListAdapter: SongsListAdapter
    private val songIdList: List<String> = listOf("song_1", "song_2", "song_3", "song_4",
        "song_5", "song_6")
    // IDs
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Fetch the first song details to set the activity header
        fetchFirstSongDetails(songIdList.first())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSongsListRecyclerView()
    }

    private fun fetchFirstSongDetails(songId: String) {
        firestore.collection("songs").document(songId).get().addOnSuccessListener { document ->
            document?.toObject(SongModel::class.java)?.let { song ->
                binding.nameTextView.text = song.title
            }
        }
    }

    private fun setupSongsListRecyclerView() {
        songsListAdapter = SongsListAdapter(songIdList)
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }
}
