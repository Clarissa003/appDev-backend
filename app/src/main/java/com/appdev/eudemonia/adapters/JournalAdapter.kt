package com.appdev.eudemonia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.dataclasses.JournalEntry
import com.appdev.eudemonia.R

class JournalAdapter(private val journalList: List<JournalEntry>) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.journal_item, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journalEntry = journalList[position]
        holder.contentTextView.text = journalEntry.content
        holder.dateTextView.text = journalEntry.date
    }

    override fun getItemCount(): Int {
        return journalList.size
    }
}
