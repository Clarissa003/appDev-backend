package com.appdev.eudemonia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appdev.eudemonia.R
import com.appdev.eudemonia.databinding.FragmentJournalBinding
import com.appdev.eudemonia.services.HuggingFaceService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class JournalFragment : Fragment() {
    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebase()
        setupViews()
        setupGoToUnguidedJournalButton()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setupViews() {
        binding.generatePromptButton.setOnClickListener {
            generatePrompt()
        }

        binding.saveButton.setOnClickListener {
            saveJournalEntry()
        }
    }

    private fun generatePrompt() {
        val apiKey = "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"
        val inputs = "Write about one positive thing that happened today."

        HuggingFaceService().getGeneratedPrompt(apiKey, inputs) { prompt ->
            binding.displayPrompt.text = prompt
        }
    }

    private fun setupGoToUnguidedJournalButton() {
        binding.gotoUnguidedJournalButton.setOnClickListener {
            findNavController().navigate(R.id.action_journalFragment_to_unguidedJournalFragment)
        }
    }

    private fun saveJournalEntry() {
        val content = binding.enterAnswer.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (content.isNotEmpty() && userId != null) {
            val currentDate = dateFormat.format(Date())

            val journalEntry = hashMapOf(
                "content" to content,
                "date" to currentDate,
                "userId" to userId
            )

            db.collection("Journal")
                .add(journalEntry)
                .addOnSuccessListener {
                    handleSaveSuccess()
                }
                .addOnFailureListener { e ->
                    handleSaveFailure(e.message)
                }
        } else {
            handleValidationErrors(content, userId)
        }
    }

    private fun handleSaveSuccess() {
        Toast.makeText(requireContext(), "Entry saved!", Toast.LENGTH_SHORT).show()
        binding.enterAnswer.text.clear()
    }

    private fun handleSaveFailure(errorMessage: String?) {
        Toast.makeText(requireContext(), "Error saving entry: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun handleValidationErrors(content: String, userId: String?) {
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Content cannot be empty", Toast.LENGTH_SHORT).show()
        }
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
