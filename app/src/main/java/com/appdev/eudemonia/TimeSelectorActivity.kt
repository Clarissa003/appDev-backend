package com.appdev.eudemonia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.concurrent.TimeUnit
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class TimeSelectorActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setTimeButton: Button
    private val CHANNEL_ID = "affirmation_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_picker)

        timePicker = findViewById(R.id.timePicker)
        setTimeButton = findViewById(R.id.button_set_time)

        createNotificationChannel()

        setTimeButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            scheduleDailyNotification(hour, minute)
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            Toast.makeText(this@TimeSelectorActivity, "Notification set for $timeString daily", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Affirmation"
            val descriptionText = "Channel for daily affirmation notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleDailyNotification(hour: Int, minute: Int) {
        val currentTime = Calendar.getInstance()
        val notificationTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (notificationTime.before(currentTime)) {
            notificationTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = notificationTime.timeInMillis - currentTime.timeInMillis
        val dailyWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_affirmation")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_affirmation_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest as PeriodicWorkRequest
        )
    }
}

class NotificationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    private val CHANNEL_ID = "affirmation_channel"
    private val firestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        fetchRandomAffirmation { affirmation ->
            sendNotification(affirmation)
        }
        return Result.success()
    }

    private fun fetchRandomAffirmation(callback: (String) -> Unit) {
        firestore.collection("Affirmations")
            .get()
            .addOnSuccessListener { result ->
                val affirmations = result.documents.mapNotNull { it.getString("Affirmation") }
                val randomAffirmation = affirmations.randomOrNull() ?: "Stay positive!"
                callback(randomAffirmation)
            }
            .addOnFailureListener {
                callback("Stay positive!")
            }
    }

    private fun sendNotification(affirmation: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Affirmation")
            .setContentText(affirmation)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }
}