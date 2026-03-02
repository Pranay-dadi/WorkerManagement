package com.example.workermanagement

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.databinding.ActivityMainBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.example.workermanagement.ui.manager.LoginManagerActivity
import com.example.workermanagement.ui.worker.LoginWorkerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Auto-login if session exists
        checkExistingSession()

        // Animate elements on entry
        animateEntrance()

        binding.btnWorker.setOnClickListener {
            startActivity(Intent(this, LoginWorkerActivity::class.java))
        }

        binding.btnManager.setOnClickListener {
            startActivity(Intent(this, LoginManagerActivity::class.java))
        }
    }

    private fun checkExistingSession() {
        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getString(FirebaseRepository.KEY_USER_ID, null)
        val userType = prefs.getString(FirebaseRepository.KEY_USER_TYPE, null)

        if (!userId.isNullOrEmpty() && !userType.isNullOrEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                val target = when (userType) {
                    "worker" -> SearchActivity::class.java
                    "manager" -> ManageActivity::class.java
                    else -> return@postDelayed
                }
                startActivity(Intent(this, target))
                finish()
            }, 500)
        }
    }

    private fun animateEntrance() {
        val fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        val fadeSlideUp2 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up).apply {
            startOffset = 150
        }
        val fadeSlideUp3 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up).apply {
            startOffset = 300
        }

        binding.tvAppTitle.startAnimation(fadeSlideUp)
        binding.tvTagline.startAnimation(fadeSlideUp2)
        binding.cardButtons.startAnimation(fadeSlideUp3)
    }
}