package com.appdev.eudemonia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.eudemonia.R
import com.appdev.eudemonia.adapters.Mood
import com.appdev.eudemonia.adapters.MoodAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_moods, container, false)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val happyButton: ImageView = view.findViewById(R.id.happyButton)
        val contentButton: ImageView = view.findViewById(R.id.contentButton)
        val neutralButton: ImageView = view.findViewById(R.id.neutralButton)
        val unhappyButton: ImageView = view.findViewById(R.id.unhappyButton)
        val sadButton: ImageView = view.findViewById(R.id.sadButton)

        happyButton.setOnClickListener { saveMoodToDb("happy") }
        contentButton.setOnClickListener { saveMoodToDb("content") }
        neutralButton.setOnClickListener { saveMoodToDb("neutral") }
        unhappyButton.setOnClickListener { saveMoodToDb("unhappy") }
        sadButton.setOnClickListener { saveMoodToDb("sad") }

        return view
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
                fetchMoods()
                Toast.makeText(context, "Mood saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error saving mood", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMoods() {
        val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val userId = mAuth.currentUser?.uid ?: return

        db.collection("Mood")
            .whereEqualTo("userId", userId)
            .whereEqualTo("dateAdded", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                val moodList = mutableListOf<Mood>()
                for (document in result) {
                    val mood = document.toObject(Mood::class.java)
                    moodList.add(mood)
                }
                updateMoodRecyclerView(moodList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching moods", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateMoodRecyclerView(moodList: List<Mood>) {
        val moodRecyclerView: RecyclerView = view?.findViewById(R.id.moodRecyclerView) ?: return
        moodRecyclerView.layoutManager = LinearLayoutManager(context)
        val moodAdapter = MoodAdapter(moodList)
        moodRecyclerView.adapter = moodAdapter
    }
}
