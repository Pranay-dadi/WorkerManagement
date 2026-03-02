package com.example.workermanagement.ui.manager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.R
import com.example.workermanagement.data.ManagerData
import com.example.workermanagement.databinding.ActivityRegisterManagerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar

class RegisterManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterManagerBinding.inflate(layoutInflater)
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
        val orgTypes = resources.getStringArray(R.array.organization_types)
        binding.actvOrgType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, orgTypes)
        )
        val states = resources.getStringArray(R.array.indian_states)
        binding.actvState.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, states)
        )
    }

    private fun validateForm(): Boolean {
        var valid = true
        binding.tilUsername.error = if (binding.etUsername.text.isNullOrBlank()) { valid = false; "Required" } else null
        binding.tilPassword.error = if (binding.etPassword.text.isNullOrBlank()) { valid = false; "Required" } else null
        binding.tilName.error = if (binding.etName.text.isNullOrBlank()) { valid = false; "Required" } else null
        binding.tilPhone.error = if (binding.etPhone.text.toString().length != 10) { valid = false; "10 digits required" } else null
        binding.tilOrgName.error = if (binding.etOrgName.text.isNullOrBlank()) { valid = false; "Required" } else null
        binding.tilCity.error = if (binding.etCity.text.isNullOrBlank()) { valid = false; "Required" } else null
        binding.tilPincode.error = if (binding.etPincode.text.toString().length != 6) { valid = false; "6 digits required" } else null

        val pwd = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        if (pwd.isNotEmpty() && pwd != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else binding.tilConfirmPassword.error = null
        return valid
    }

    private fun submitRegistration() {
        setLoading(true)
        val manager = ManagerData(
            username = binding.etUsername.text.toString().trim().lowercase(),
            password = binding.etPassword.text.toString(),
            name = binding.etName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            dateOfBirth = binding.etDob.text.toString().trim(),
            organizationName = binding.etOrgName.text.toString().trim(),
            organizationType = binding.actvOrgType.text.toString().trim(),
            organizationAddress = binding.etOrgAddress.text.toString().trim(),
            organizationCity = binding.etCity.text.toString().trim(),
            organizationState = binding.actvState.text.toString().trim(),
            organizationPincode = binding.etPincode.text.toString().trim(),
            gstNumber = binding.etGst.text.toString().trim().uppercase(),
            registrationNumber = binding.etRegNo.text.toString().trim().uppercase(),
            website = binding.etWebsite.text.toString().trim(),
            alternatePhone = binding.etAltPhone.text.toString().trim()
        )
        FirebaseRepository.registerManager(
            manager = manager,
            onSuccess = {
                setLoading(false)
                Snackbar.make(binding.root, "Registration successful! Please log in.", Snackbar.LENGTH_LONG).show()
                startActivity(Intent(this, LoginManagerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
            },
            onError = { msg ->
                setLoading(false)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.error))
                    .setTextColor(getColor(R.color.white)).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}