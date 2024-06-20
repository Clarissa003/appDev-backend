package com.appdev.eudemonia

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
            handler.postDelayed(this, 24 * 60 * 60 * 1000) // Schedule again in 10 minutes
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler(HandlerThread("DeleteOldMessagesThread").apply { start() }.looper)
        db = FirebaseFirestore.getInstance()
        Log.d("com.appdev.eudemonia.DeleteOldMessagesService", "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("com.appdev.eudemonia.DeleteOldMessagesService", "Service onStartCommand")
        handler.post(deleteOldMessagesTask) // Start task immediately
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(deleteOldMessagesTask) // Remove pending tasks
        Log.d("com.appdev.eudemonia.DeleteOldMessagesService", "Service onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun deleteOldMessages() {
        val currentTimeMillis = System.currentTimeMillis()
        val tenMinutesInMillis = 10 * 60 * 1000  // 10 minutes in milliseconds
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
                        .addOnSuccessListener {
                            Log.d("com.appdev.eudemonia.DeleteOldMessagesService", "Deleted ${querySnapshot.size()} old messages successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("com.appdev.eudemonia.DeleteOldMessagesService", "Error committing batch delete", e)
                        }
                } else {
                    Log.d("com.appdev.eudemonia.DeleteOldMessagesService", "No old messages to delete")
                }
            }
            .addOnFailureListener { e ->
                Log.e("com.appdev.eudemonia.DeleteOldMessagesService", "Error querying old messages", e)
            }
    }

}