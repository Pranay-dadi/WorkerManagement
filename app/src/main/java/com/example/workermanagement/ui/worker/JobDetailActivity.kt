package com.example.workermanagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.data.ApplicationData
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ActivityJobDetailBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding
    private var jobId: String = ""
    private var currentJob: WorkData? = null
    private var hasApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        jobId = intent.getStringExtra("jobId") ?: run { finish(); return }

        binding.btnBack.setOnClickListener { finish() }

        loadJobDetails()
        checkIfAlreadyApplied()
    }

    private fun loadJobDetails() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseRepository.jobsRef.child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    val job = snapshot.getValue(WorkData::class.java) ?: return
                    currentJob = job
                    populateUI(job)
                }
                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    showError("Failed to load job: ${error.message}")
                }
            })
    }

    private fun populateUI(job: WorkData) {
        // Header
        binding.tvTitle.text = job.title
        binding.tvOrganization.text = job.organizationName.ifEmpty { job.managerName }
        binding.tvCategory.text = job.category
        binding.tvLocation.text = buildLocationString(job)
        binding.tvPostedDate.text = "Posted ${getTimeAgo(job.postedDate)}"

        // Urgent badge
        binding.tvUrgent.visibility = if (job.isUrgent) View.VISIBLE else View.GONE

        // Wage
        binding.tvWage.text = "₹${job.wage}"
        binding.tvWageType.text = "per ${job.wageType.lowercase()}"

        // Description
        if (job.description.isNotEmpty()) {
            binding.tvDescription.text = job.description
            binding.layoutDescription.visibility = View.VISIBLE
        }

        // Positions
        binding.tvPositionsAvailable.text = "${job.availablePositions} of ${job.totalPositions} positions open"
        binding.progressPositions.max = job.totalPositions
        binding.progressPositions.progress = job.filledPositions

        // Schedule
        if (job.startDate.isNotEmpty()) binding.tvStartDate.text = job.startDate
        if (job.endDate.isNotEmpty()) binding.tvEndDate.text = job.endDate
        binding.tvDuration.text = job.durationType.replace("_", " ").uppercase()
        binding.tvWorkingHours.text = "${job.workingHours} hrs/day"
        binding.tvWorkingDays.text = job.workingDays

        // Requirements
        if (job.skillsRequired.isNotEmpty()) {
            binding.tvSkills.text = job.skillsRequired
        }
        binding.tvExperience.text = if (job.experienceYears == 0) "No experience required"
        else "${job.experienceYears}+ years"
        binding.tvAgeRange.text = "${job.minAge} - ${job.maxAge} years"
        binding.tvGender.text = job.genderPreference.uppercase().let {
            if (it == "ANY") "Any gender" else it + " only"
        }
        if (job.languageRequired.isNotEmpty()) {
            binding.tvLanguage.text = job.languageRequired
            binding.rowLanguage.visibility = View.VISIBLE
        }
        if (job.physicalRequirements.isNotEmpty()) {
            binding.tvPhysicalReq.text = job.physicalRequirements
            binding.rowPhysical.visibility = View.VISIBLE
        }

        // Benefits
        binding.tvAccommodation.text = if (job.accommodationProvided) "✅ Accommodation provided" else "❌ No accommodation"
        binding.tvMeals.text = if (job.mealsProvided) "✅ Meals provided" else "❌ No meals"
        binding.tvTransport.text = if (job.transportProvided) "✅ Transport provided" else "❌ No transport"
        binding.tvTools.text = if (job.toolsProvided) "✅ Tools provided"
        else if (job.ownToolsRequired) "⚠️ Bring your own tools" else "Tools not specified"

        // Contact
        binding.tvContactPerson.text = job.contactPerson.ifEmpty { "Manager" }
        binding.tvContactPhone.text = "+91 ${job.phone}"
        binding.tvContactEmail.text = job.email.ifEmpty { "—" }

        // Call button
        if (job.phone.isNotEmpty()) {
            binding.btnCall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.phone}")))
            }
            binding.btnCall.visibility = View.VISIBLE
        }

        // Apply button
        binding.btnApply.setOnClickListener { applyForJob(job) }
        binding.btnApply.isEnabled = job.isActive && !job.isFullyFilled
        if (job.isFullyFilled) {
            binding.btnApply.text = "All Positions Filled"
            binding.btnApply.alpha = 0.5f
        }
    }

    private fun checkIfAlreadyApplied() {
        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        val workerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: return

        FirebaseRepository.applicationsRef
            .orderByChild("jobId").equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasApplied = snapshot.children
                        .mapNotNull { it.getValue(ApplicationData::class.java) }
                        .any { it.workerId == workerId && it.status != "withdrawn" }

                    if (hasApplied) {
                        binding.btnApply.text = getString(R.string.applied)
                        binding.btnApply.isEnabled = false
                        binding.btnApply.alpha = 0.6f
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun applyForJob(job: WorkData) {
        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        val workerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: run {
            showError("Please log in to apply"); return
        }
        val workerName = prefs.getString(FirebaseRepository.KEY_USER_NAME, "") ?: ""

        binding.btnApply.isEnabled = false
        binding.btnApply.text = "Applying..."

        val application = ApplicationData(
            jobId = job.id,
            jobTitle = job.title,
            workerId = workerId,
            workerName = workerName,
            managerId = job.managerId,
            organizationName = job.organizationName
        )

        FirebaseRepository.submitApplication(
            application = application,
            onSuccess = {
                binding.btnApply.text = getString(R.string.applied)
                Snackbar.make(binding.root, "Application submitted successfully! 🎉", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.success))
                    .setTextColor(getColor(R.color.white))
                    .show()
            },
            onError = { msg ->
                binding.btnApply.isEnabled = true
                binding.btnApply.text = getString(R.string.apply_now)
                showError(msg)
            }
        )
    }

    private fun buildLocationString(job: WorkData): String {
        val parts = mutableListOf<String>()
        if (job.address.isNotEmpty()) parts.add(job.address)
        if (job.city.isNotEmpty()) parts.add(job.city)
        if (job.state.isNotEmpty()) parts.add(job.state)
        if (job.pincode.isNotEmpty()) parts.add(job.pincode)
        return parts.joinToString(", ").ifEmpty { "Location not specified" }
    }

    private fun getTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < TimeUnit.HOURS.toMillis(1) -> "just now"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}