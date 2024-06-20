package com.appdev.eudemonia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.databinding.ActivityPlayerBinding
import com.bumptech.glide.Glide
import androidx.media3.exoplayer.ExoPlayer

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = MyExoplayer.getInstance()
        binding.playerView.player = exoPlayer

        MyExoplayer.getCurrentSong()?.let { song ->
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            Glide.with(this).load(song.coverUrl).circleCrop().into(binding.songCoverImageView)
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

    override fun onDestroy() {
        super.onDestroy()
        MyExoplayer.releasePlayer()
    }
}
