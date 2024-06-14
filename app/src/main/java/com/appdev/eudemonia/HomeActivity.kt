package com.appdev.eudemonia

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var selectedDateTextView: TextView
    private lateinit var calendar: Calendar
    private val currentUserEmail = Firebase.auth.currentUser?.email

    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Ensure this is your consolidated layout file

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        // Calendar setup
        selectedDateTextView = findViewById(R.id.idTVSelectedDate)
        val pickDateButton: Button = findViewById(R.id.idBtnPickDate)
        calendar = Calendar.getInstance()
        updateLabel()

        pickDateButton.setOnClickListener {
            showDatePicker()
        }

        // Moods setup
        val happyButton: ImageView = findViewById(R.id.happyButton)
        val contentButton: ImageView = findViewById(R.id.contentButton)
        val neutralButton: ImageView = findViewById(R.id.neutralButton)
        val unhappyButton: ImageView = findViewById(R.id.unhappyButton)
        val sadButton: ImageView = findViewById(R.id.sadButton)

        happyButton.setOnClickListener { saveMoodToDb("happy") }
        contentButton.setOnClickListener { saveMoodToDb("content") }
        neutralButton.setOnClickListener { saveMoodToDb("neutral") }
        unhappyButton.setOnClickListener { saveMoodToDb("unhappy") }
        sadButton.setOnClickListener { saveMoodToDb("sad") }

        // Habits setup
        habitRecyclerView = findViewById(R.id.habitRecyclerView)
        habitRecyclerView.layoutManager = LinearLayoutManager(this)

        // Call fetchHabits() before setting up the adapter
        habitList.clear() // Clear the list before fetching habits
        fetchHabits() // Fetch habits before setting up RecyclerView adapter

        habitAdapter = HabitAdapter(habitList)
        habitRecyclerView.adapter = habitAdapter


        val buttonAddHabit: Button = findViewById(R.id.buttonAddHabit)
        buttonAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun updateLabel() {
        val myFormat = "dd-MM-yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        selectedDateTextView.text = dateFormat.format(calendar.time)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this@HomeActivity,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel()
                fetchHabits()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
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
                Toast.makeText(this, "Mood saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving mood", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddHabitDialog() {
        val dialog = AlertDialog.Builder(this)
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
                Toast.makeText(this, "Habit added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchHabits() {
        Log.d("HomeActivity", "MartijnchHabits() called")
        try{
            Log.d("HomeActivity", "fetchHabits() called")
            val selectedDate = selectedDateTextView.text.toString()
            Log.d("HomeActivity", "Selected Date: $selectedDate")
            currentUserEmail?.let { email ->
                db.collection("User")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val userId = documents.first().id
                        Log.d("HomeActivity", "User ID: $userId")
                        db.collection("Habit")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("dateAdded", selectedDate) // Filter by selected date
                            .get()
                            .addOnSuccessListener { result ->
                                habitList.clear()
                                for (document in result) {
                                    val habit = document.toObject(Habit::class.java)
                                    habitList.add(habit)
                                }
                                habitAdapter.notifyDataSetChanged()
                                Log.d("HomeActivity", "Habits fetched: ${habitList.size}")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error fetching habits: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("HomeActivity", "Error fetching habits: ${e.message}", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("HomeActivity", "Error fetching user: ${e.message}", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Exception in fetchHabits(): ${e.message}", e)
        }
    }

}
