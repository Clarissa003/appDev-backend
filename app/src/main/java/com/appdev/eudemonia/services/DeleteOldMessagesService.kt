package com.appdev.eudemonia.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.Date

class DeleteOldMessagesService : Service() {

    private lateinit var handler: Handler
    private lateinit var db: FirebaseFirestore

    private val deleteOldMessagesTask = object : Runnable {
        override fun run() {
            deleteOldMessages()
            handler.postDelayed(this, 24 * 60 * 60 * 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler(HandlerThread("DeleteOldMessagesThread").apply { start() }.looper)
        db = FirebaseFirestore.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(deleteOldMessagesTask)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(deleteOldMessagesTask)
        Log.d("com.appdev.eudemonia.services.DeleteOldMessagesService", "Service onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun deleteOldMessages() {
        val currentTimeMillis = System.currentTimeMillis()
        val tenMinutesInMillis = 24 * 60 * 60 * 1000
        val thresholdTimeMillis = currentTimeMillis - tenMinutesInMillis

        val thresholdTimestamp = Timestamp(Date(thresholdTimeMillis))

        db.collection("Message")
            .whereLessThan("timestamp", thresholdTimestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = db.batch()
                    querySnapshot.documents.forEach { document ->
                        batch.delete(document.reference)
                    }
                    batch.commit()
                }
            }
    }

}