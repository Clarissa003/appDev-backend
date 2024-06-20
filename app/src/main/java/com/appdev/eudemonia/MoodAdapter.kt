package com.appdev.eudemonia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodAdapter(private val moodList: List<Mood>) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_item, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moodList[position]
        holder.bind(mood)
    }

    override fun getItemCount(): Int = moodList.size

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodNameTextView: TextView = itemView.findViewById(R.id.moodNameTextView)

        fun bind(mood: Mood) {
            moodNameTextView.text = mood.name
        }
    }
}
