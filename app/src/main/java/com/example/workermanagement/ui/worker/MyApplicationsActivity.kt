package com.example.workermanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workermanagement.data.ApplicationData
import com.example.workermanagement.databinding.ActivityMyApplicationsBinding
import com.example.workermanagement.databinding.ItemApplicationWorkerBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyApplicationsBinding
    private lateinit var adapter: WorkerApplicationAdapter
    private var dbListener: ValueEventListener? = null
    private var workerId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences(FirebaseRepository.PREFS_NAME, MODE_PRIVATE)
        workerId = prefs.getString(FirebaseRepository.KEY_USER_ID, "") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        adapter = WorkerApplicationAdapter(emptyList()) { app ->
            withdrawApplication(app)
        }
        binding.rvApplications.layoutManager = LinearLayoutManager(this)
        binding.rvApplications.adapter = adapter

        loadApplications()
    }

    private fun loadApplications() {
        binding.progressBar.visibility = View.VISIBLE
        dbListener = FirebaseRepository.listenWorkerApplications(
            workerId = workerId,
            onUpdate = { apps ->
                binding.progressBar.visibility = View.GONE
                adapter.updateData(apps)
                binding.layoutEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
                binding.tvCount.text = "${apps.size} application${if (apps.size != 1) "s" else ""}"
            },
            onError = { msg ->
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error: $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun withdrawApplication(app: ApplicationData) {
        if (app.status != "pending") {
            Snackbar.make(binding.root, "Only pending applications can be withdrawn.", Snackbar.LENGTH_SHORT).show()
            return
        }
        FirebaseRepository.updateApplicationStatus(
            appId = app.id,
            status = "withdrawn",
            managerNote = "",
            onSuccess = {
                Snackbar.make(binding.root, "Application withdrawn.", Snackbar.LENGTH_SHORT).show()
            },
            onError = { msg ->
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener?.let { FirebaseRepository.applicationsRef.removeEventListener(it) }
    }
}

// ──────────────────────────────────────────────────────────────
// Inline adapter for worker's applications list
// ──────────────────────────────────────────────────────────────
class WorkerApplicationAdapter(
    private var apps: List<ApplicationData>,
    private val onWithdraw: (ApplicationData) -> Unit
) : RecyclerView.Adapter<WorkerApplicationAdapter.VH>() {

    inner class VH(val binding: ItemApplicationWorkerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemApplicationWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        val b = holder.binding

        b.tvJobTitle.text = app.jobTitle
        b.tvOrganization.text = app.organizationName
        b.tvAppliedDate.text = "Applied ${formatDate(app.appliedDate)}"
        b.tvManagerNote.text = app.managerNote.ifEmpty { "No response yet" }
        b.tvManagerNote.alpha = if (app.managerNote.isEmpty()) 0.5f else 1f

        // Status chip
        val (statusText, statusColor) = when (app.status) {
            "pending"   -> "⏳ Pending"  to R.color.status_pending
            "accepted"  -> "✅ Accepted" to R.color.status_accepted
            "rejected"  -> "❌ Rejected" to R.color.status_rejected
            "withdrawn" -> "↩ Withdrawn" to R.color.status_withdrawn
            else        -> app.status.uppercase() to R.color.text_hint
        }
        b.tvStatus.text = statusText
        b.tvStatus.setTextColor(holder.itemView.context.getColor(statusColor))

        b.btnWithdraw.visibility = if (app.status == "pending") View.VISIBLE else View.GONE
        b.btnWithdraw.setOnClickListener { onWithdraw(app) }
    }

    private fun formatDate(ts: Long): String {
        if (ts == 0L) return ""
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ts))
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