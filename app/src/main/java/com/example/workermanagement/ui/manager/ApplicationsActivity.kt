package com.example.workermanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workermanagement.data.ApplicationData
import com.example.workermanagement.databinding.ActivityApplicationsBinding
import com.example.workermanagement.databinding.ItemApplicationManagerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationsBinding
    private lateinit var adapter: ManagerApplicationAdapter
    private var dbListener: ValueEventListener? = null
    private var jobId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        jobId = intent.getStringExtra("jobId") ?: run { finish(); return }
        val jobTitle = intent.getStringExtra("jobTitle") ?: "Job"

        binding.tvJobTitle.text = jobTitle
        binding.btnBack.setOnClickListener { finish() }

        adapter = ManagerApplicationAdapter(
            apps = emptyList(),
            onAccept = { app -> updateStatus(app, "accepted") },
            onReject = { app -> showRejectDialog(app) }
        )
        binding.rvApplications.layoutManager = LinearLayoutManager(this)
        binding.rvApplications.adapter = adapter

        loadApplications()
    }

    private fun loadApplications() {
        binding.progressBar.visibility = View.VISIBLE
        dbListener = FirebaseRepository.listenJobApplications(
            jobId = jobId,
            onUpdate = { apps ->
                binding.progressBar.visibility = View.GONE
                adapter.updateData(apps)
                updateStats(apps)
                binding.layoutEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = { msg ->
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error: $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun updateStats(apps: List<ApplicationData>) {
        binding.tvTotalCount.text = apps.size.toString()
        binding.tvPendingCount.text = apps.count { it.status == "pending" }.toString()
        binding.tvAcceptedCount.text = apps.count { it.status == "accepted" }.toString()
        binding.tvRejectedCount.text = apps.count { it.status == "rejected" }.toString()
    }

    private fun updateStatus(app: ApplicationData, status: String, note: String = "") {
        FirebaseRepository.updateApplicationStatus(
            appId = app.id,
            status = status,
            managerNote = note,
            onSuccess = {
                val msg = if (status == "accepted") "Application accepted!" else "Application rejected."
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(if (status == "accepted") R.color.success else R.color.error))
                    .setTextColor(getColor(R.color.white))
                    .show()
            },
            onError = { msg -> Snackbar.make(binding.root, "Error: $msg", Snackbar.LENGTH_LONG).show() }
        )
    }

    private fun showRejectDialog(app: ApplicationData) {
        val input = android.widget.EditText(this).apply {
            hint = "Reason for rejection (optional)"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("Reject Application")
            .setMessage("Reject ${app.workerName}'s application?")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                updateStatus(app, "rejected", input.text.toString().trim())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener?.let { FirebaseRepository.applicationsRef.removeEventListener(it) }
    }
}

// ──────────────────────────────────────────────────────────────
// Inline adapter for manager's application list
// ──────────────────────────────────────────────────────────────
class ManagerApplicationAdapter(
    private var apps: List<ApplicationData>,
    private val onAccept: (ApplicationData) -> Unit,
    private val onReject: (ApplicationData) -> Unit
) : RecyclerView.Adapter<ManagerApplicationAdapter.VH>() {

    inner class VH(val binding: ItemApplicationManagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemApplicationManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        val b = holder.binding
        val ctx = holder.itemView.context

        b.tvWorkerName.text = app.workerName
        b.tvWorkerPhone.text = "+91 ${app.workerPhone}"
        b.tvWorkerSkills.text = app.workerSkills.ifEmpty { "No skills listed" }
        b.tvWorkerExp.text = if (app.workerExperienceYears == 0) "Fresher" else "${app.workerExperienceYears} yrs exp"
        b.tvAppliedDate.text = "Applied ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(app.appliedDate))}"
        if (app.workerNote.isNotEmpty()) {
            b.tvWorkerNote.text = "\"${app.workerNote}\""
            b.tvWorkerNote.visibility = View.VISIBLE
        }

        val (statusText, statusColor) = when (app.status) {
            "pending"   -> "⏳ Pending"  to R.color.status_pending
            "accepted"  -> "✅ Accepted" to R.color.status_accepted
            "rejected"  -> "❌ Rejected" to R.color.status_rejected
            "withdrawn" -> "↩ Withdrawn" to R.color.status_withdrawn
            else        -> app.status to R.color.text_hint
        }
        b.tvStatus.text = statusText
        b.tvStatus.setTextColor(ctx.getColor(statusColor))

        val isPending = app.status == "pending"
        b.btnAccept.visibility = if (isPending) View.VISIBLE else View.GONE
        b.btnReject.visibility = if (isPending) View.VISIBLE else View.GONE

        b.btnAccept.setOnClickListener { onAccept(app) }
        b.btnReject.setOnClickListener { onReject(app) }
    }

    fun updateData(newApps: List<ApplicationData>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = apps.size
            override fun getNewListSize() = newApps.size
            override fun areItemsTheSame(o: Int, n: Int) = apps[o].id == newApps[n].id
            override fun areContentsTheSame(o: Int, n: Int) = apps[o] == newApps[n]
        })
        apps = newApps
        diff.dispatchUpdatesTo(this)
    }
}