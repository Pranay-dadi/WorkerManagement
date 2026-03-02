package com.example.workermanagement.ui.manager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.ManageActivity
import com.example.workermanagement.R
import com.example.workermanagement.databinding.ActivityLoginManagerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar

class LoginManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInputs(username, password)) return@setOnClickListener

            setLoading(true)

            FirebaseRepository.loginManager(
                username = username,
                password = password,
                onSuccess = { manager ->
                    // ✅ FIX: Firebase callbacks fire on a background thread.
                    //    All UI operations MUST run on the main thread.
                    runOnUiThread {
                        getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE).edit()
                            .putString(FirebaseRepository.KEY_USER_ID, manager.id)
                            .putString(FirebaseRepository.KEY_USER_TYPE, "manager")
                            .putString(FirebaseRepository.KEY_USER_NAME, manager.name)
                            .putString(FirebaseRepository.KEY_ORG_NAME, manager.organizationName)
                            .apply()

                        setLoading(false)

                        startActivity(Intent(this, ManageActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }
                },
                onError = { msg ->
                    // ✅ FIX: Also wrap error callback in runOnUiThread
                    runOnUiThread {
                        setLoading(false)
                        showError(msg)
                    }
                }
            )
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterManagerActivity::class.java))
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        var valid = true
        binding.tilUsername.error = if (username.isEmpty()) { valid = false; "Username required" } else null
        binding.tilPassword.error = if (password.isEmpty()) { valid = false; "Password required" } else null
        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}