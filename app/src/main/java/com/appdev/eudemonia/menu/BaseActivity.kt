package com.appdev.eudemonia.menu

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.appdev.eudemonia.R
import com.appdev.eudemonia.chat.FriendListActivity
import com.appdev.eudemonia.friends.FriendsActivity
import com.appdev.eudemonia.home.HomeActivity
import com.appdev.eudemonia.journals.GuidedJournalActivity
import com.appdev.eudemonia.journals.UnguidedJournalActivity
import com.appdev.eudemonia.profile.ProfileActivity
import com.appdev.eudemonia.settings.SettingsActivity
import com.appdev.eudemonia.songs.SongsListActivity


open class BaseActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }

            R.id.unguidedJournal -> {
                startActivity(Intent(this, UnguidedJournalActivity::class.java))
                return true
            }

            R.id.guidedJournal -> {
                startActivity(Intent(this, GuidedJournalActivity::class.java))
                return true
            }
            R.id.calendar -> {
                startActivity(Intent(this, HomeActivity::class.java))
                return true
            }

            R.id.calendar -> {
                startActivity(Intent(this, HomeActivity::class.java))
                return true
            }

            R.id.friendList -> {
                startActivity(Intent(this, FriendListActivity::class.java))
                return true
            }

            R.id.sounds -> {
                startActivity(Intent(this, SongsListActivity::class.java))
                return true
            }

            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }

            R.id.addFriends -> {
                startActivity(Intent(this, FriendsActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
