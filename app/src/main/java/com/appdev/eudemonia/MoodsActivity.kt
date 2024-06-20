package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodsActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moods)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

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
    }

    private fun saveMoodToDb(moodName: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val mood = hashMapOf(
            "name" to moodName,
            "userId" to userId
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
}
