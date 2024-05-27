package com.appdev.eudemonia

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

        private lateinit var selectedDateTextView: TextView
        private lateinit var calendar: Calendar
        private val db = FirebaseFirestore.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_calendar)

            selectedDateTextView = findViewById(R.id.idTVSelectedDate)
            val pickDateButton: Button = findViewById(R.id.idBtnPickDate)

            calendar = Calendar.getInstance()

            // Set the default date to the current date
            updateLabel()

            pickDateButton.setOnClickListener {
                DatePickerDialog(
                    this@CalendarActivity,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        private val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
            fetchDataFromFirebase()
        }

        private fun updateLabel() {
            val myFormat = "yyyy-MM-dd" // Date format
            val dateFormat = SimpleDateFormat(myFormat, Locale.US)
            selectedDateTextView.text = dateFormat.format(calendar.time)
        }

        private fun fetchDataFromFirebase() {
            val selectedDate = selectedDateTextView.text.toString()

            // Fetch data from Journal collection
            db.collection("Journal").whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val journalName = document.getString("name")
                        val username = document.getString("username")
                        // Handle your UI update here
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle possible errors
                }

            // Fetch data from Moods collection
            db.collection("Moods").whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val moodName = document.getString("name")
                        val username = document.getString("username")
                        // Handle your UI update here
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle possible errors
                }

            // Fetch data from Habits collection
            db.collection("Habits").whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val habitName = document.getString("name")
                        val username = document.getString("username")
                        // Handle your UI update here
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle possible errors
                }
        }
}
