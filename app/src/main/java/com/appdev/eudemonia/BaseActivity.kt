package com.appdev.eudemonia

import com.appdev.eudemonia.R
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


open class BaseActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_menu, menu)
        return true
    }

/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.profile -> {
                // Navigate to Profile page
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }

            R.id.moods -> {
                // Navigate to Moods page
                startActivity(Intent(this, MoodsActivity::class.java))
                return true
            }

            R.id.habit -> {
                // Navigate to Habit page
                startActivity(Intent(this, HabitsActivity::class.java))
                return true
            }

            R.id.unguidedJournal -> {
                // Navigate to Unguided Journal page
                startActivity(Intent(this, UnguidedJournalActivity::class.java))
                return true
            }

            R.id.guidedJournal -> {
                // Navigate to Guided Journal page
                startActivity(Intent(this, GuidedJournalActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_menu)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_guided_journal -> {
                    val intent = Intent(this, GuidedJournalActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_friends -> {
                    val intent = Intent(this, FriendsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_sounds -> {
                    val intent = Intent(this, SoundsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Set default selection
        bottomNavigation.selectedItemId = R.id.navigation_home
    }
}
