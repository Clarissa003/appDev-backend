package com.appdev.eudemonia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R

class MoodsAdapter(private val moodList: List<Mood>) :
    RecyclerView.Adapter<MoodsAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_single_mood, parent, false)
        return MoodViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val currentMood = moodList[position]
        holder.bind(currentMood)
    }

    override fun getItemCount() = moodList.size

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodImageView: ImageView = itemView.findViewById(R.id.moodImageView)

        fun bind(mood: Mood) {
            // Assuming you have a method to get the drawable resource ID based on mood
            moodImageView.setImageResource(getMoodDrawableId(mood))
        }

        private fun getMoodDrawableId(mood: Mood): Int {
            return when (mood.name) {
                "Happy" -> R.drawable.happy
                "Content" -> R.drawable.content
                "Neutral" -> R.drawable.neutral
                "Unhappy" -> R.drawable.unhappy
                "Sad" -> R.drawable.sad
                // No default case
                else -> throw IllegalArgumentException("Unknown mood: ${mood.name}")
            }
        }
    }
}
