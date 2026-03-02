package com.example.workermanagement.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.R
import com.example.workermanagement.SearchActivity
import com.example.workermanagement.databinding.ActivityLoginWorkerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar

class LoginWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginWorkerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInputs(username, password)) return@setOnClickListener

            setLoading(true)

            FirebaseRepository.loginWorker(
                username = username,
                password = password,
                onSuccess = { worker ->
                    // ✅ FIX: Firebase callbacks fire on a background thread.
                    //    All UI operations (startActivity, setText, setVisibility)
                    //    MUST be dispatched back to the main thread via runOnUiThread.
                    runOnUiThread {
                        getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE).edit()
                            .putString(FirebaseRepository.KEY_USER_ID, worker.id)
                            .putString(FirebaseRepository.KEY_USER_TYPE, "worker")
                            .putString(FirebaseRepository.KEY_USER_NAME, worker.fullName.ifEmpty { worker.username })
                            .apply()

                        setLoading(false)

                        startActivity(Intent(this, SearchActivity::class.java).apply {
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
            startActivity(Intent(this, RegisterWorkerActivity::class.java))
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        var valid = true
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            valid = false
        } else {
            binding.tilUsername.error = null
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            valid = false
        } else {
            binding.tilPassword.error = null
        }
        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (loading) "" else getString(R.string.login)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}