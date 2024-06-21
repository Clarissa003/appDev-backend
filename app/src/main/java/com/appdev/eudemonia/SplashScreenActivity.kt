package com.appdev.eudemonia

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val splashImageView: View = findViewById(R.id.iv_splash)

        // Create an alpha animation
        val alphaAnimation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 2000 // duration in milliseconds
            fillAfter = true // keep the final state after the animation
        }

        // Start the animation
        splashImageView.startAnimation(alphaAnimation)

        // Duration of wait (total time for splash screen to be visible)
        val splashScreenDuration = 3000L // 3 seconds

        // Handler to start the main activity and close this splash activity after some seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Start main activity
            startActivity(Intent(this, SignupActivity::class.java))
            // Close splash activity
            finish()
        }, splashScreenDuration)
    }
}
