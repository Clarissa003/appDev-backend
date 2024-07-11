package com.appdev.eudemonia.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.HabitAdapter
import com.appdev.eudemonia.adapters.JournalAdapter
import com.appdev.eudemonia.adapters.Mood
import com.appdev.eudemonia.adapters.MoodAdapter
import com.appdev.eudemonia.dataclasses.Habit
import com.appdev.eudemonia.dataclasses.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var selectedDateTextView: TextView
    private lateinit var calendar: Calendar
    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()
    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initializeFirebase()
        initializeViews(view)
        setupDatePicker(view)
        initializeMoods(view)
        initializeHabits(view)
        return view
    }

    private fun initializeFirebase() {
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initializeViews(view: View) {
        selectedDateTextView = view.findViewById(R.id.idTVSelectedDate)
    }

    private fun setupDatePicker(view: View) {
        calendar = Calendar.getInstance()
        updateLabel()

        val pickDateButton: Button = view.findViewById(R.id.idBtnPickDate)
        pickDateButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun updateLabel() {
        val myFormat = "dd-MM-yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        selectedDateTextView.text = dateFormat.format(calendar.time)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel()
                fetchHabits()
                fetchMoods()
                fetchJournalEntries()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun initializeMoods(view: View) {
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView)
        moodRecyclerView.layoutManager = LinearLayoutManager(context)

        moodList.clear()
        fetchMoods()

        moodAdapter = MoodAdapter(moodList)
        moodRecyclerView.adapter = moodAdapter

        setupMoodButtons(view)
    }

    private fun setupMoodButtons(view: View) {
        val happyButton: ImageView = view.findViewById(R.id.happyButton)
        val contentButton: ImageView = view.findViewById(R.id.contentButton)
        val neutralButton: ImageView = view.findViewById(R.id.neutralButton)
        val unhappyButton: ImageView = view.findViewById(R.id.unhappyButton)
        val sadButton: ImageView = view.findViewById(R.id.sadButton)

        happyButton.setOnClickListener { saveMoodToDb("Happy") }
        contentButton.setOnClickListener { saveMoodToDb("Content") }
        neutralButton.setOnClickListener { saveMoodToDb("Neutral") }
        unhappyButton.setOnClickListener { saveMoodToDb("Unhappy") }
        sadButton.setOnClickListener { saveMoodToDb("Sad") }
    }

    private fun saveMoodToDb(moodName: String) {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val userId = mAuth.currentUser?.uid ?: return
        val mood = hashMapOf(
            "name" to moodName,
            "userId" to userId,
            "dateAdded" to currentDate
        )
        db.collection("Mood")
            .add(mood)
            .addOnSuccessListener {
                fetchMoods()
                Toast.makeText(context, "Mood saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error saving mood", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeHabits(view: View) {
        habitRecyclerView = view.findViewById(R.id.habitRecyclerView)
        habitRecyclerView.layoutManager = LinearLayoutManager(context)

        habitList.clear()
        fetchHabits()

        habitAdapter = HabitAdapter(habitList)
        habitRecyclerView.adapter = habitAdapter

        setupAddHabitButton(view)
    }

    private fun setupAddHabitButton(view: View) {
        val buttonAddHabit: Button = view.findViewById(R.id.buttonAddHabit)
        buttonAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.activity_single_habit, null)
        val habitNameEditText: EditText = dialogView.findViewById(R.id.habitNameEditText)

        dialog.setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val habitName = habitNameEditText.text.toString()
                if (habitName.isNotEmpty()) {
                    saveHabitToDb(habitName)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun saveHabitToDb(habitName: String) {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val userId = mAuth.currentUser?.uid ?: return
        val habit = hashMapOf(
            "name" to habitName,
            "userId" to userId,
            "dateAdded" to currentDate
        )
        db.collection("Habit")
            .add(habit)
            .addOnSuccessListener {
                fetchHabits()
                Toast.makeText(context, "Habit added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error adding habit", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchHabits() {
        val selectedDate = selectedDateTextView.text.toString()
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
                        .whereEqualTo("dateAdded", selectedDate)
                        .get()
                        .addOnSuccessListener { result ->
                            habitList.clear()
                            for (document in result) {
                                val habit = document.toObject(Habit::class.java)
                                habitList.add(habit)
                            }
                            habitAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error fetching habits", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error fetching user", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchMoods() {
        val selectedDate = selectedDateTextView.text.toString()
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
                    db.collection("Mood")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("dateAdded", selectedDate)
                        .get()
                        .addOnSuccessListener { result ->
                            moodList.clear()
                            for (document in result) {
                                val mood = document.toObject(Mood::class.java)
                                moodList.add(mood)
                            }
                            moodAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error fetching moods", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error fetching user", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchJournalEntries() {
        val selectedDate = selectedDateTextView.text.toString()
        val userId = mAuth.currentUser?.uid ?: return

        db.collection("Journal")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                val journalList = mutableListOf<JournalEntry>()
                for (document in result) {
                    val journalEntry = document.toObject(JournalEntry::class.java)
                    journalList.add(journalEntry)
                }
                updateJournalRecyclerView(journalList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching journal entries", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateJournalRecyclerView(journalList: List<JournalEntry>) {
        val journalRecyclerView: RecyclerView = view?.findViewById(R.id.JournalRecyclerView) ?: return
        journalRecyclerView.layoutManager = LinearLayoutManager(context)
        val journalAdapter = JournalAdapter(journalList)
        journalRecyclerView.adapter = journalAdapter
    }
}
