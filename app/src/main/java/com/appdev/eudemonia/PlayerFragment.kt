package com.appdev.eudemonia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import com.appdev.eudemonia.databinding.FragmentPlayerBinding
import com.appdev.eudemonia.objects.MyExoplayer

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private var exoPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = MyExoplayer.getInstance()
        binding.playerView.player = exoPlayer

        MyExoplayer.getCurrentSong()?.let { song ->
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
        }
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
