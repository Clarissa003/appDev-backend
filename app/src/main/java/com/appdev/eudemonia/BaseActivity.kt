package com.appdev.eudemonia

import com.appdev.eudemonia.R
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.profile -> {
                // Navigate to Profile page
                startActivity(Intent(this, ProfileActivity::class.java))
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

            R.id.calendar -> {
                // Navigate to Guided Journal page
                startActivity(Intent(this, HomeActivity::class.java))
                return true
            }

            R.id.friends -> {
                startActivity(Intent(this, FriendsActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
