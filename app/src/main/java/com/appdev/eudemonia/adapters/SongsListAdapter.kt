package com.appdev.eudemonia.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.databinding.SongListItemRecyclerRowBinding
import com.appdev.eudemonia.models.SongModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class SongsListAdapter(private var songsList: List<SongModel>, private val clickListener: (SongModel) -> Unit) :
    RecyclerView.Adapter<SongsListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(song: SongModel, clickListener: (SongModel) -> Unit) {
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            Glide.with(binding.songCoverImageView.context)
                .load(song.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
            binding.root.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("songTitle", song.title)
                    putString("songSubtitle", song.subtitle)
                }
                it.findNavController().navigate(R.id.action_navigation_songs_to_playerFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songsList[position], clickListener)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    fun updateSongsList(newSongsList: List<SongModel>) {
        songsList = newSongsList
        notifyDataSetChanged()
    }
}
