package com.appdev.eudemonia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.SongsListAdapter
import com.appdev.eudemonia.databinding.FragmentSongsListBinding
import com.appdev.eudemonia.models.SongModel

class SongsListFragment : Fragment() {

    private var _binding: FragmentSongsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var songsListAdapter: SongsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter with an empty list and a click listener
        songsListAdapter = SongsListAdapter(emptyList()) { song ->
            // Handle the click event, e.g., navigate to another fragment
            val bundle = Bundle().apply {
                putString("songTitle", song.title)
                putString("songSubtitle", song.subtitle)
            }
            findNavController().navigate(R.id.action_navigation_songs_to_playerFragment, bundle)
        }

        // Set up the RecyclerView
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songsListRecyclerView.adapter = songsListAdapter

        // Load songs into the adapter
        loadSongs()
    }

    private fun loadSongs() {
        // Load your songs data here and update the adapter
        val sampleSongs = listOf(
            SongModel("Title 1", "Subtitle 1", "url1"),
            SongModel("Title 2", "Subtitle 2", "url2"),
            // Add more songs as needed
        )
        songsListAdapter.updateSongsList(sampleSongs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
