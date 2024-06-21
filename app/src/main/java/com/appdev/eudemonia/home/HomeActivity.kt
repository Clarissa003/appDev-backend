package com.appdev.eudemonia.home

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.menu.BaseActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.HabitAdapter
import com.appdev.eudemonia.adapters.JournalAdapter
import com.appdev.eudemonia.adapters.Mood
import com.appdev.eudemonia.adapters.MoodAdapter
import com.appdev.eudemonia.authentication.LoginActivity
import com.appdev.eudemonia.chat.FriendListActivity
import com.appdev.eudemonia.dataclasses.Habit
import com.appdev.eudemonia.dataclasses.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private val myPreferences: SharedPreferences by lazy {
        getSharedPreferences("myPref", Context.MODE_PRIVATE)
    }

    private lateinit var selectedDateTextView: TextView
    private lateinit var calendar: Calendar
    private var currentUserEmail = Firebase.auth.currentUser?.email

    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()
    private val CHANNEL_ID = "crisis_channel"


    class HomeActivity : BaseActivity() {

        private lateinit var db: FirebaseFirestore
        private lateinit var mAuth: FirebaseAuth
        private lateinit var selectedDateTextView: TextView
        private lateinit var calendar: Calendar
        private val currentUserEmail = Firebase.auth.currentUser?.email

        private lateinit var habitRecyclerView: RecyclerView
        private lateinit var habitAdapter: HabitAdapter
        private val habitList = mutableListOf<Habit>()

        private lateinit var moodRecyclerView: RecyclerView
        private lateinit var moodAdapter: MoodAdapter
        private val moodList = mutableListOf<Mood>()
        private val CHANNEL_ID = "crisis_channel"


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_home) // Ensure this is your consolidated layout file

            Log.d("HomeActivity", "onCreate called")

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
            moodRecyclerView = findViewById(R.id.moodRecyclerView)
            moodRecyclerView.layoutManager = LinearLayoutManager(this)

            Log.d("HomeActivity", "Before fetching moods")

            moodList.clear()
            fetchMoods() // Ensure this line is present

            moodAdapter = MoodAdapter(moodList)
            moodRecyclerView.adapter = moodAdapter

            val happyButton: ImageView = findViewById(R.id.happyButton)
            val contentButton: ImageView = findViewById(R.id.contentButton)
            val neutralButton: ImageView = findViewById(R.id.neutralButton)
            val unhappyButton: ImageView = findViewById(R.id.unhappyButton)
            val sadButton: ImageView = findViewById(R.id.sadButton)

            happyButton.setOnClickListener { saveMoodToDb("Happy") }
            contentButton.setOnClickListener { saveMoodToDb("Content") }
            neutralButton.setOnClickListener { saveMoodToDb("Neutral") }
            unhappyButton.setOnClickListener { saveMoodToDb("Unhappy") }
            sadButton.setOnClickListener { saveMoodToDb("Sad") }

            createNotificationChannel()

            // Habits setup
            habitRecyclerView = findViewById(R.id.habitRecyclerView)
            habitRecyclerView.layoutManager = LinearLayoutManager(this)

            habitList.clear() // Clear the list before fetching habits
            fetchHabits() // Fetch habits before setting up RecyclerView adapter

            habitAdapter = HabitAdapter(habitList)
            habitRecyclerView.adapter = habitAdapter

            val buttonAddHabit: Button = findViewById(R.id.buttonAddHabit)
            buttonAddHabit.setOnClickListener {
                showAddHabitDialog()
            }

            //Journal setup
            fetchJournalEntries()

            Log.d("HomeActivity", "onCreate finished")
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
                    fetchMoods()
                    fetchJournalEntries()
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
                .addOnSuccessListener { documentReference ->
                    fetchMoods()
                    Toast.makeText(this, "Mood saved", Toast.LENGTH_SHORT).show()

                    if (moodName == "Sad") {
                        sendNotification()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving mood", Toast.LENGTH_SHORT).show()
                    Log.e("HomeActivity", "Error saving mood", e)
                }
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Crisis notification"
                val descriptionText = "Crisis notification, chat with someone"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun sendNotification() {
            val intent = Intent(applicationContext, FriendListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val requestCode = System.currentTimeMillis().toInt()
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sad?")
                .setContentText("Chat with one of your friends!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1001, builder.build())
        }


        private fun fetchMoods() {
            Log.d("HomeActivity", "fetchMoods called")
            val selectedDate = selectedDateTextView.text.toString()
            Log.d("HomeActivity", "Selected date: $selectedDate")
            currentUserEmail?.let { email ->
                Log.d("HomeActivity", "Current user email: $email")
                db.collection("User")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d("HomeActivity", "User documents: ${documents.size()}")
                        if (documents.isEmpty) {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val userId = documents.first().id
                        Log.d("HomeActivity", "User ID: $userId")
                        db.collection("Mood")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("dateAdded", selectedDate)
                            .get()
                            .addOnSuccessListener { result ->
                                Log.d("HomeActivity", "Moods fetched: ${result.size()}")
                                moodList.clear()
                                for (document in result) {
                                    val mood = document.toObject(Mood::class.java)
                                    moodList.add(mood)
                                }
                                moodAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error fetching moods: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("HomeActivity", "Error fetching moods: ${e.message}", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error fetching user: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("HomeActivity", "Error fetching user: ${e.message}", e)
                    }
            } ?: run {
                Log.e("HomeActivity", "currentUserEmail is null")
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
                    Toast.makeText(this, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
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
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val userId = documents.first().id
                        Log.d("HomeActivity", "User ID: $userId")
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
                                Log.d("HomeActivity", "Habits fetched: ${habitList.size}")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error fetching habits: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("HomeActivity", "Error fetching habits: ${e.message}", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error fetching user: ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("HomeActivity", "Error fetching user: ${e.message}", e)
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
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error fetching journal entries: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        private fun updateJournalRecyclerView(journalList: List<JournalEntry>) {
            val journalRecyclerView: RecyclerView = findViewById(R.id.JournalRecyclerView)
            journalRecyclerView.layoutManager = LinearLayoutManager(this)
            val journalAdapter = JournalAdapter(journalList)
            journalRecyclerView.adapter = journalAdapter
        }

    }
}
