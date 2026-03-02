package com.example.workermanagement.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.R
import com.example.workermanagement.data.WorkerData
import com.example.workermanagement.databinding.ActivityRegisterWorkerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar

class RegisterWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterWorkerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupDropdowns()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            submitRegistration()
        }
    }

    private fun setupDropdowns() {
        val categories = resources.getStringArray(R.array.job_categories)
        binding.actvPrimarySkill.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        )

        val states = resources.getStringArray(R.array.indian_states)
        binding.actvState.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, states)
        )

        val wagePref = arrayOf("Daily", "Weekly", "Monthly", "Fixed Contract")
        binding.actvWagePreference.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, wagePref)
        )
    }

    private fun validateForm(): Boolean {
        var valid = true
        val checks = listOf(
            binding.etUsername to binding.tilUsername to "Username required",
            binding.etPassword to binding.tilPassword to "Password required",
            binding.etFullName to binding.tilFullName to "Full name required",
            binding.etPhone to binding.tilPhone to "Phone number required",
            binding.etCity to binding.tilCity to "City required",
            binding.etPincode to binding.tilPincode to "Pincode required",
        )
        // Flatten and validate
        binding.tilUsername.error = if (binding.etUsername.text.isNullOrBlank()) { valid = false; "Username required" } else null
        binding.tilPassword.error = if (binding.etPassword.text.isNullOrBlank()) { valid = false; "Password required" } else null
        binding.tilFullName.error = if (binding.etFullName.text.isNullOrBlank()) { valid = false; "Full name required" } else null
        binding.tilPhone.error = if (binding.etPhone.text.toString().length != 10) { valid = false; "Enter valid 10-digit number" } else null
        binding.tilCity.error = if (binding.etCity.text.isNullOrBlank()) { valid = false; "City required" } else null
        binding.tilPincode.error = if (binding.etPincode.text.toString().length != 6) { valid = false; "Enter valid 6-digit pincode" } else null
        binding.tilAadhar.error = if (binding.etAadhar.text.toString().length != 12) { valid = false; "Enter valid 12-digit Aadhar" } else null

        val pwd = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        if (pwd.isNotEmpty() && pwd != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        return valid
    }

    private fun submitRegistration() {
        setLoading(true)
        val worker = WorkerData(
            username = binding.etUsername.text.toString().trim().lowercase(),
            password = binding.etPassword.text.toString(),
            fullName = binding.etFullName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            dateOfBirth = binding.etDob.text.toString().trim(),
            gender = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                else -> "Other"
            },
            address = binding.etAddress.text.toString().trim(),
            city = binding.etCity.text.toString().trim(),
            state = binding.actvState.text.toString().trim(),
            pincode = binding.etPincode.text.toString().trim(),
            aadharNumber = binding.etAadhar.text.toString().trim(),
            primarySkill = binding.actvPrimarySkill.text.toString().trim(),
            skills = binding.etSkills.text.toString().trim(),
            experienceYears = binding.etExperience.text.toString().toIntOrNull() ?: 0,
            expectedWage = binding.etExpectedWage.text.toString().trim(),
            preferredWageType = binding.actvWagePreference.text.toString().trim().lowercase(),
            willingToRelocate = binding.cbRelocate.isChecked,
            hasAadhar = binding.etAadhar.text.toString().length == 12,
            hasBankAccount = binding.cbBankAccount.isChecked,
            bankAccountNumber = binding.etBankAccount.text.toString().trim(),
            ifscCode = binding.etIfsc.text.toString().trim().uppercase()
        )

        FirebaseRepository.registerWorker(
            worker = worker,
            onSuccess = {
                setLoading(false)
                Snackbar.make(binding.root, "Registration successful! Please log in.", Snackbar.LENGTH_LONG).show()
                startActivity(Intent(this, LoginWorkerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
            },
            onError = { msg ->
                setLoading(false)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.error))
                    .setTextColor(getColor(R.color.white))
                    .show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}