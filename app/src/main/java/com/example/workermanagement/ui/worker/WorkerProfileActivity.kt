package com.example.workermanagement

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.data.WorkerData
import com.example.workermanagement.databinding.ActivityWorkerProfileBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerProfileBinding
    private var workerId = ""
    private var currentWorker: WorkerData? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        workerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener { toggleEditMode() }
        binding.btnSave.setOnClickListener { saveProfile() }

        setupDropdowns()
        loadProfile()
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
    }

    private fun loadProfile() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseRepository.workersRef.child(workerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    val worker = snapshot.getValue(WorkerData::class.java) ?: return
                    currentWorker = worker
                    populateForm(worker)
                }
                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
                }
            })
    }

    private fun populateForm(w: WorkerData) {
        binding.tvUsername.text = "@${w.username}"
        binding.tvJoinedDate.text = "Member since ${formatEpoch(w.registrationDate)}"

        binding.etFullName.setText(w.fullName)
        binding.etPhone.setText(w.phone)
        binding.etEmail.setText(w.email)
        binding.etDob.setText(w.dateOfBirth)
        binding.etAddress.setText(w.address)
        binding.etCity.setText(w.city)
        binding.actvState.setText(w.state, false)
        binding.etPincode.setText(w.pincode)
        binding.actvPrimarySkill.setText(w.primarySkill, false)
        binding.etSkills.setText(w.skills)
        binding.etExperience.setText(w.experienceYears.toString())
        binding.etExpectedWage.setText(w.expectedWage)
        binding.cbRelocate.isChecked = w.willingToRelocate
        binding.cbBankAccount.isChecked = w.hasBankAccount
        binding.etBankAccount.setText(w.bankAccountNumber)
        binding.etIfsc.setText(w.ifscCode)

        // Stats
        binding.tvJobsCompleted.text = w.totalJobsCompleted.toString()
        binding.tvRating.text = if (w.rating > 0) String.format("%.1f", w.rating) else "—"

        setEditMode(false)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        setEditMode(isEditMode)
    }

    private fun setEditMode(edit: Boolean) {
        val fields = listOf(
            binding.etFullName, binding.etPhone, binding.etEmail,
            binding.etDob, binding.etAddress, binding.etCity,
            binding.etPincode, binding.etSkills, binding.etExperience,
            binding.etExpectedWage, binding.etBankAccount, binding.etIfsc
        )
        fields.forEach { it.isEnabled = edit }
        binding.actvPrimarySkill.isEnabled = edit
        binding.actvState.isEnabled = edit
        binding.cbRelocate.isEnabled = edit
        binding.cbBankAccount.isEnabled = edit

        binding.btnEdit.visibility = if (edit) View.GONE else View.VISIBLE
        binding.btnSave.visibility = if (edit) View.VISIBLE else View.GONE
        binding.btnCancel.visibility = if (edit) View.VISIBLE else View.GONE

        binding.btnCancel.setOnClickListener {
            isEditMode = false
            currentWorker?.let { populateForm(it) }
        }
    }

    private fun saveProfile() {
        val phone = binding.etPhone.text.toString()
        if (phone.length != 10) {
            binding.tilPhone.error = "Valid 10-digit number required"
            return
        }

        binding.btnSave.isEnabled = false

        val updates = mapOf(
            "fullName" to binding.etFullName.text.toString().trim(),
            "phone" to phone,
            "email" to binding.etEmail.text.toString().trim(),
            "dateOfBirth" to binding.etDob.text.toString().trim(),
            "address" to binding.etAddress.text.toString().trim(),
            "city" to binding.etCity.text.toString().trim(),
            "state" to binding.actvState.text.toString().trim(),
            "pincode" to binding.etPincode.text.toString().trim(),
            "primarySkill" to binding.actvPrimarySkill.text.toString().trim(),
            "skills" to binding.etSkills.text.toString().trim(),
            "experienceYears" to (binding.etExperience.text.toString().toIntOrNull() ?: 0),
            "expectedWage" to binding.etExpectedWage.text.toString().trim(),
            "willingToRelocate" to binding.cbRelocate.isChecked,
            "hasBankAccount" to binding.cbBankAccount.isChecked,
            "bankAccountNumber" to binding.etBankAccount.text.toString().trim(),
            "ifscCode" to binding.etIfsc.text.toString().trim().uppercase()
        )

        FirebaseRepository.workersRef.child(workerId).updateChildren(updates)
            .addOnSuccessListener {
                binding.btnSave.isEnabled = true
                isEditMode = false
                setEditMode(false)
                // Update session name
                getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(FirebaseRepository.KEY_USER_NAME, updates["fullName"] as String)
                    .apply()
                Snackbar.make(binding.root, "Profile updated successfully!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(R.color.success))
                    .setTextColor(getColor(R.color.white))
                    .show()
            }
            .addOnFailureListener {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, "Update failed: ${it.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.error))
                    .setTextColor(getColor(R.color.white))
                    .show()
            }
    }

    private fun formatEpoch(ts: Long): String {
        if (ts == 0L) return "unknown"
        val sdf = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ts))
    }
}