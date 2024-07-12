package com.appdev.eudemonia.menu

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.appdev.eudemonia.R
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Retrieve NavHostFragment first and then get NavController from it
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        Log.d("BaseActivity", "NavController: $navController")
        Log.d("BaseActivity", "BottomNavigationView: $bottomNavigationView")

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            Log.d("BaseActivity", "Menu item selected: ${item.title}")
            val handled = when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.navigation_friends -> {
                    navController.navigate(R.id.navigation_friends)
                    true
                }

                R.id.navigation_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }

                R.id.navigation_journals -> {
                    navController.navigate(R.id.journalFragment)
                    true
                }

                R.id.navigation_songs -> {
                    navController.navigate(R.id.soundsFragment)
                    true
                }

                else -> false
            }
            if (!handled) {
                Log.e("BaseActivity", "Navigation to ${item.title} failed")
            }
            handled
        }
    }
}
