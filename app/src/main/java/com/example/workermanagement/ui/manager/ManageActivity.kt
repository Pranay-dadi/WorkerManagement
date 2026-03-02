package com.example.workermanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workermanagement.adapter.JobManageAdapter
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ActivityManageBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ValueEventListener

class ManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageBinding
    private lateinit var adapter: JobManageAdapter
    private var dbListener: ValueEventListener? = null
    private var managerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        managerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: ""
        val managerName = prefs.getString(FirebaseRepository.KEY_USER_NAME, "Manager") ?: "Manager"
        val orgName = prefs.getString(FirebaseRepository.KEY_ORG_NAME, "") ?: ""

        binding.tvWelcome.text = "Welcome, $managerName"
        binding.tvOrgName.text = orgName

        setupRecyclerView()
        loadJobs()

        binding.fabAddJob.setOnClickListener {
            startActivity(Intent(this, PostWorkActivity::class.java))
        }

        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun setupRecyclerView() {
        adapter = JobManageAdapter(
            jobs = emptyList(),
            onEditClick = { job ->
                startActivity(Intent(this, PostWorkActivity::class.java).apply {
                    putExtra("editJobId", job.id)
                })
            },
            onDeleteClick = { job -> confirmDelete(job) },
            onToggleStatus = { job ->
                FirebaseRepository.toggleJobStatus(job.id, !job.isActive) {
                    val msg = if (!job.isActive) "Job activated" else "Job paused"
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                }
            },
            onViewApplications = { job ->
                startActivity(Intent(this, ApplicationsActivity::class.java).apply {
                    putExtra("jobId", job.id)
                    putExtra("jobTitle", job.title)
                })
            }
        )
        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = adapter
    }

    private fun loadJobs() {
        binding.progressBar.visibility = View.VISIBLE
        dbListener = FirebaseRepository.listenManagerJobs(
            managerId = managerId,
            onUpdate = { jobs ->
                binding.progressBar.visibility = View.GONE
                adapter.updateData(jobs)
                updateStats(jobs)
                binding.layoutEmpty.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = { msg ->
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error: $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun updateStats(jobs: List<WorkData>) {
        val active = jobs.count { it.isActive }
        val totalPositions = jobs.sumOf { it.totalPositions }
        val filled = jobs.sumOf { it.filledPositions }
        val applications = jobs.sumOf { it.applicationsCount }

        binding.tvStatActive.text = active.toString()
        binding.tvStatPositions.text = "$filled / $totalPositions"
        binding.tvStatApplications.text = applications.toString()
    }

    private fun confirmDelete(job: WorkData) {
        AlertDialog.Builder(this)
            .setTitle("Delete Job Posting")
            .setMessage("Delete \"${job.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseRepository.deleteJob(
                    jobId = job.id,
                    onSuccess = {
                        Snackbar.make(binding.root, "Job deleted successfully", Snackbar.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        Snackbar.make(binding.root, "Error: $msg", Snackbar.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        dbListener?.let { FirebaseRepository.jobsRef.removeEventListener(it) }
        getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener?.let { FirebaseRepository.jobsRef.removeEventListener(it) }
    }
}