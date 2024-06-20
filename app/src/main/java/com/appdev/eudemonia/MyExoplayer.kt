package com.appdev.eudemonia

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.appdev.eudemonia.models.SongModel

object MyExoplayer {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: SongModel? = null

    fun getCurrentSong(): SongModel? {
        return currentSong
    }

    fun getInstance(): ExoPlayer? {
        return exoPlayer
    }

    fun initializePlayer(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun startPlaying(context: Context, song: SongModel) {
        if (exoPlayer == null) {
            initializePlayer(context)
        }

        if (currentSong != song) {
            currentSong = song
            currentSong?.url?.let {
                val mediaItem = MediaItem.fromUri(it)
                exoPlayer?.apply {
                    stop()
                    clearMediaItems()
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
            }
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        currentSong = null
    }
}
