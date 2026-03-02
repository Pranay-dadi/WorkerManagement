package com.example.workermanagement

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ActivityPostWorkBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PostWorkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostWorkBinding
    private var editJobId: String? = null
    private var existingJob: WorkData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        editJobId = intent.getStringExtra("editJobId")

        setupDropdowns()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPost.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            submitJob()
        }

        if (editJobId != null) {
            binding.tvScreenTitle.text = "Edit Job"
            binding.btnPost.text = "Update Job"
            loadExistingJob(editJobId!!)
        }
    }

    private fun setupDropdowns() {
        fun adapter(arr: Array<String>) = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arr)

        binding.actvCategory.setAdapter(adapter(resources.getStringArray(R.array.job_categories)))
        binding.actvState.setAdapter(adapter(resources.getStringArray(R.array.indian_states)))
        binding.actvWageType.setAdapter(adapter(arrayOf("Daily", "Weekly", "Monthly", "Fixed Contract")))
        binding.actvDurationType.setAdapter(adapter(arrayOf("Short-term (< 1 month)", "Long-term (> 1 month)", "Seasonal", "Permanent", "Contract")))
        binding.actvGender.setAdapter(adapter(arrayOf("Any", "Male", "Female")))
    }

    private fun loadExistingJob(jobId: String) {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseRepository.jobsRef.child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    existingJob = snapshot.getValue(WorkData::class.java)
                    existingJob?.let { populateForm(it) }
                }
                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, "Failed to load job: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
            })
    }

    private fun populateForm(job: WorkData) {
        binding.etTitle.setText(job.title)
        binding.actvCategory.setText(job.category, false)
        binding.etDescription.setText(job.description)
        binding.etAddress.setText(job.address)
        binding.etCity.setText(job.city)
        binding.actvState.setText(job.state, false)
        binding.etPincode.setText(job.pincode)
        binding.etWage.setText(job.wage)
        binding.actvWageType.setText(job.wageType.replaceFirstChar { it.uppercase() }, false)
        binding.cbAccommodation.isChecked = job.accommodationProvided
        binding.cbMeals.isChecked = job.mealsProvided
        binding.cbTransport.isChecked = job.transportProvided
        binding.etStartDate.setText(job.startDate)
        binding.etEndDate.setText(job.endDate)
        binding.actvDurationType.setText(job.durationType, false)
        binding.etWorkingHours.setText(job.workingHours)
        binding.etWorkingDays.setText(job.workingDays)
        binding.etSkillsRequired.setText(job.skillsRequired)
        binding.etExperienceYears.setText(job.experienceYears.toString())
        binding.etMinAge.setText(job.minAge.toString())
        binding.etMaxAge.setText(job.maxAge.toString())
        binding.actvGender.setText(job.genderPreference.replaceFirstChar { it.uppercase() }, false)
        binding.etTotalPositions.setText(job.totalPositions.toString())
        binding.etFilledPositions.setText(job.filledPositions.toString())
        binding.etPhone.setText(job.phone)
        binding.etEmail.setText(job.email)
        binding.etContactPerson.setText(job.contactPerson)
        binding.cbUrgent.isChecked = job.isUrgent
        binding.cbToolsProvided.isChecked = job.toolsProvided
        binding.cbOwnTools.isChecked = job.ownToolsRequired
    }

    private fun validateForm(): Boolean {
        var valid = true
        binding.tilTitle.error = if (binding.etTitle.text.isNullOrBlank()) { valid = false; "Job title required" } else null
        binding.tilCategory.error = if (binding.actvCategory.text.isNullOrBlank()) { valid = false; "Select a category" } else null
        binding.tilCity.error = if (binding.etCity.text.isNullOrBlank()) { valid = false; "City required" } else null
        binding.tilPincode.error = if (binding.etPincode.text.toString().length != 6) { valid = false; "Valid 6-digit pincode required" } else null
        binding.tilWage.error = if (binding.etWage.text.isNullOrBlank()) { valid = false; "Wage required" } else null
        binding.tilPhone.error = if (binding.etPhone.text.toString().length != 10) { valid = false; "Valid 10-digit phone required" } else null
        val positions = binding.etTotalPositions.text.toString().toIntOrNull()
        binding.tilTotalPositions.error = if (positions == null || positions < 1) { valid = false; "At least 1 position required" } else null
        return valid
    }

    private fun submitJob() {
        setLoading(true)
        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        val managerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: ""
        val managerName = prefs.getString(FirebaseRepository.KEY_USER_NAME, "") ?: ""
        val orgName = prefs.getString(FirebaseRepository.KEY_ORG_NAME, "") ?: ""

        val job = WorkData(
            id = editJobId ?: "",
            title = binding.etTitle.text.toString().trim(),
            category = binding.actvCategory.text.toString().trim(),
            description = binding.etDescription.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            city = binding.etCity.text.toString().trim(),
            state = binding.actvState.text.toString().trim(),
            pincode = binding.etPincode.text.toString().trim(),
            wage = binding.etWage.text.toString().trim(),
            wageType = binding.actvWageType.text.toString().trim().lowercase(),
            accommodationProvided = binding.cbAccommodation.isChecked,
            mealsProvided = binding.cbMeals.isChecked,
            transportProvided = binding.cbTransport.isChecked,
            startDate = binding.etStartDate.text.toString().trim(),
            endDate = binding.etEndDate.text.toString().trim(),
            durationType = binding.actvDurationType.text.toString().trim().lowercase().replace(" ", "_"),
            workingHours = binding.etWorkingHours.text.toString().trim().ifEmpty { "8" },
            workingDays = binding.etWorkingDays.text.toString().trim().ifEmpty { "Mon-Sat" },
            skillsRequired = binding.etSkillsRequired.text.toString().trim(),
            experienceYears = binding.etExperienceYears.text.toString().toIntOrNull() ?: 0,
            minAge = binding.etMinAge.text.toString().toIntOrNull() ?: 18,
            maxAge = binding.etMaxAge.text.toString().toIntOrNull() ?: 60,
            genderPreference = binding.actvGender.text.toString().trim().lowercase().ifEmpty { "any" },
            physicalRequirements = binding.etPhysicalReq.text.toString().trim(),
            languageRequired = binding.etLanguage.text.toString().trim(),
            toolsProvided = binding.cbToolsProvided.isChecked,
            ownToolsRequired = binding.cbOwnTools.isChecked,
            totalPositions = binding.etTotalPositions.text.toString().toIntOrNull() ?: 1,
            filledPositions = binding.etFilledPositions.text.toString().toIntOrNull()
                ?: existingJob?.filledPositions ?: 0,
            phone = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            contactPerson = binding.etContactPerson.text.toString().trim(),
            managerId = managerId,
            managerName = managerName,
            organizationName = orgName,
            isUrgent = binding.cbUrgent.isChecked,
            isActive = true
        )

        if (editJobId != null) {
            FirebaseRepository.updateJob(
                job = job,
                onSuccess = {
                    setLoading(false)
                    Snackbar.make(binding.root, "Job updated successfully!", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onError = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error)).setTextColor(getColor(R.color.white)).show()
                }
            )
        } else {
            FirebaseRepository.postJob(
                job = job,
                onSuccess = {
                    setLoading(false)
                    Snackbar.make(binding.root, "Job posted successfully!", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onError = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error)).setTextColor(getColor(R.color.white)).show()
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnPost.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}