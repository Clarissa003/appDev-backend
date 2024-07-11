package com.appdev.eudemonia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.HabitAdapter
import com.appdev.eudemonia.dataclasses.Habit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {

    private val db = Firebase.firestore
    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()
    private val currentUserEmail = Firebase.auth.currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        habitRecyclerView = view.findViewById(R.id.habitRecyclerView)
        habitRecyclerView.layoutManager = LinearLayoutManager(context)
        habitAdapter = HabitAdapter(habitList)
        habitRecyclerView.adapter = habitAdapter

        val buttonAddHabit: Button = view.findViewById(R.id.buttonAddHabit)
        buttonAddHabit.setOnClickListener {
            showAddHabitDialog()
        }

        fetchHabits()
        return view
    }

    private fun showAddHabitDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.activity_single_habit, null)
        val habitNameEditText: EditText = dialogView.findViewById(R.id.habitNameEditText)

        dialog.setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val habitName = habitNameEditText.text.toString()
                if (habitName.isNotEmpty()) {
                    currentUserEmail?.let { email ->
                        fetchUserIdAndAddHabit(habitName, email)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun fetchUserIdAndAddHabit(habitName: String, email: String) {
        db.collection("User")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val userId = documents.first().id
                addHabitToFirestore(habitName, userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addHabitToFirestore(habitName: String, userId: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val habit = Habit(name = habitName, dateAdded = currentDate, userId = userId)

        db.collection("Habit")
            .add(habit)
            .addOnSuccessListener {
                fetchHabits() // Fetch the updated list after adding a new habit
                Toast.makeText(context, "Habit added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchHabits() {
        currentUserEmail?.let { email ->
            db.collection("User")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    val userId = documents.first().id
                    db.collection("Habit")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { result ->
                            habitList.clear()
                            for (document in result) {
                                val habit = document.toObject(Habit::class.java)
                                habitList.add(habit)
                            }
                            habitAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error fetching habits: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
