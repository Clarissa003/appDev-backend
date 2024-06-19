package com.appdev.eudemonia

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.MyExoplayer
import com.appdev.eudemonia.PlayerActivity
import com.appdev.eudemonia.databinding.SongListItemRecyclerRowBinding
import com.appdev.eudemonia.models.SongModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class SongsListAdapter(private var songsList: List<SongModel>) : RecyclerView.Adapter<SongsListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(song: SongModel) {
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            Glide.with(binding.songCoverImageView.context)
                .load(song.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
            binding.root.setOnClickListener {
                MyExoplayer.startPlaying(binding.root.context, song)
                it.context.startService(Intent(it.context, PlayerActivity::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songsList[position])
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    fun updateSongsList(newSongsList: List<SongModel>) {
        songsList = newSongsList
        notifyDataSetChanged()
    }
}
