package com.appdev.eudemonia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import com.appdev.eudemonia.R
import com.appdev.eudemonia.databinding.FragmentPlayerBinding
import com.appdev.eudemonia.objects.MyExoplayer

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private var exoPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val songTitle = arguments?.getString("songTitle")
        val songSubtitle = arguments?.getString("songSubtitle")

        initializePlayer(songTitle, songSubtitle)
    }

    private fun initializePlayer(title: String?, subtitle: String?) {
        exoPlayer = MyExoplayer.getInstance()
        binding.playerView.player = exoPlayer

        binding.songTitleTextView.text = title
        binding.songSubtitleTextView.text = subtitle
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyExoplayer.releasePlayer()
    }
}
