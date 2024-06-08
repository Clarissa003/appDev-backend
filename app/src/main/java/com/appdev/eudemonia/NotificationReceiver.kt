package com.appdev.eudemonia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val affirmation = intent.getStringExtra("affirmation")
        val notificationId = 1

        val builder = NotificationCompat.Builder(context, "affirmation_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Affirmation")
            .setContentText(affirmation)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
