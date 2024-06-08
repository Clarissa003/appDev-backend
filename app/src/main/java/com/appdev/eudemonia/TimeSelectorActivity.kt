package com.appdev.eudemonia

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class TimeSelectorActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_picker)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val buttonSetTime = findViewById<Button>(R.id.button_set_time)

        buttonSetTime.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            fetchAndScheduleNotification(hour, minute)
        }

        // Create the notification channel
        createNotificationChannel()
    }

    private fun fetchAndScheduleNotification(hour: Int, minute: Int) {
        firestore.collection("Affirmations").get()
            .addOnSuccessListener { result ->
                val Affirmations = result.documents.map { it.getString("text") ?: "" }
                val randomAffirmation = Affirmations.randomOrNull()

                scheduleNotification(hour, minute, randomAffirmation)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun scheduleNotification(hour: Int, minute: Int, affirmation: String?) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("affirmation", affirmation)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("affirmation_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
