package com.appdev.eudemonia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(private val habitList: MutableList<Habit>) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitNameTextView: TextView = itemView.findViewById(R.id.habitNameEditText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_single_habit, parent, false)
        return HabitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val currentHabit = habitList[position]
        holder.habitNameTextView.text = currentHabit.name
    }

    override fun getItemCount() = habitList.size

    fun updateHabits(newHabits: List<Habit>) {
        habitList.clear()
        habitList.addAll(newHabits)
        notifyDataSetChanged()
    }
}
