package com.appdev.eudemonia.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity

import com.appdev.eudemonia.R
import com.appdev.eudemonia.authentication.SignupActivity

class SplashScreenActivity : AppCompatActivity() {

    private val splashScreenDuration = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        startSplashAnimation()
        navigateToNextScreenAfterDelay()
    }

    private fun startSplashAnimation() {
        val splashImageView: View = findViewById(R.id.iv_splash)
        val alphaAnimation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 2000
            fillAfter = true
        }
        splashImageView.startAnimation(alphaAnimation)
    }

    private fun navigateToNextScreenAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }, splashScreenDuration)
    }
}
