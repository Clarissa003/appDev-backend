package com.appdev.eudemonia

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.QuerySnapshot
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

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()

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

        happyButton.setOnClickListener { saveMoodToDb("happy") }
        contentButton.setOnClickListener { saveMoodToDb("content") }
        neutralButton.setOnClickListener { saveMoodToDb("neutral") }
        unhappyButton.setOnClickListener { saveMoodToDb("unhappy") }
        sadButton.setOnClickListener { saveMoodToDb("sad") }

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

        // Journal setup
        fetchJournalEntries()
        fetchFriends { friendIds ->
            // Handle fetching friends here if needed
        }

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
                if (moodName == "sad") {
                    fetchFriends { friendIds ->
                        friendIds.forEach { friendId ->
                            fetchUsername(friendId) { username ->
                                sendCrisisNotification(friendId, username)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving mood", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFriends(callback: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            db.collection("Friends")
                .whereEqualTo("addedBy", currentUserUid)
                .get()
                .addOnSuccessListener { addedBySnapshot ->
                    handleFriendsSnapshot(addedBySnapshot, "userId", callback)
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeActivity", "Error fetching friends (addedBy): ", exception)
                }

            db.collection("Friends")
                .whereEqualTo("userId", currentUserUid)
                .get()
                .addOnSuccessListener { userIdSnapshot ->
                    handleFriendsSnapshot(userIdSnapshot, "addedBy", callback)
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeActivity", "Error fetching friends (userId): ", exception)
                }
        } else {
            Log.e("HomeActivity", "Current user UID is null")
        }
    }

    private fun handleFriendsSnapshot(snapshot: QuerySnapshot, idField: String, callback: (List<String>) -> Unit) {
        val friendIds = mutableListOf<String>()
        for (document in snapshot) {
            val friendId = document.getString(idField)
            friendId?.let {
                friendIds.add(it)
            }
        }
        callback(friendIds)
    }

    private fun fetchUsername(userId: String, callback: (String) -> Unit) {
        db.collection("User")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    callback(username)
                } else {
                    Log.e("HomeActivity", "User document not found for ID: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeActivity", "Error fetching username: ", exception)
            }
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
                        Toast.makeText(this@HomeActivity, "User not found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    val userId = documents.documents[0].id
                    Log.d("HomeActivity", "User ID: $userId")
                    db.collection("Mood")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("dateAdded", selectedDate)
                        .get()
                        .addOnSuccessListener { result ->
                            Log.d("HomeActivity", "Moods fetched: ${result.size()}")
                            moodList.clear()
                            for (document in result.documents) {
                                val mood = document.toObject(Mood::class.java)
                                if (mood != null) {
                                    moodList.add(mood)
                                }
                            }
                            moodAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@HomeActivity, "Error fetching moods: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("HomeActivity", "Error fetching moods: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@HomeActivity, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val userId = mAuth.currentUser?.uid ?: return
        val habit = hashMapOf(
            "name" to habitName,
            "userId" to userId
        )
        db.collection("Habit")
            .add(habit)
            .addOnSuccessListener { documentReference ->
                fetchHabits()
                Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving habit", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchHabits() {
        val selectedDate = selectedDateTextView.text.toString()
        Log.d("HomeActivity", "fetchHabits called for date: $selectedDate")
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
                            Toast.makeText(this, "Error fetching habits: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("HomeActivity", "Error fetching habits: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeActivity", "Error fetching user: ${e.message}", e)
                }
        } ?: run {
            Log.e("HomeActivity", "currentUserEmail is null")
        }
    }

    private fun fetchJournalEntries() {
        // Implement fetching journal entries if needed
    }

    private fun sendCrisisNotification(friendId: String, username: String) {
        // Check if VIBRATE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed with notification creation
            createNotificationChannel()

            val intent = Intent(this, GuidedJournalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("userId", friendId)
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Friend in crisis!")
                .setContentText("$username is not feeling well, have a chat.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(friendId.hashCode(), notificationBuilder.build())
                Log.d("HomeActivity", "Notification sent to $username")
            }
        } else {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.VIBRATE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Friend Notifications"
            val descriptionText = "Channel for Friend Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "friend_channel"
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
