package com.example.workermanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.workermanagement.R
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ItemJobManageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class JobManageAdapter(
    private var jobs: List<WorkData>,
    private val onEditClick: (WorkData) -> Unit,
    private val onDeleteClick: (WorkData) -> Unit,
    private val onToggleStatus: (WorkData) -> Unit,
    private val onViewApplications: (WorkData) -> Unit
) : RecyclerView.Adapter<JobManageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemJobManageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: WorkData) {
            binding.tvTitle.text = job.title
            binding.tvCategory.text = job.category
            binding.tvWage.text = "₹${job.wage}"
            binding.tvWageType.text = job.wageType.lowercase()
            binding.tvPositions.text = "${job.filledPositions}/${job.totalPositions}"
            binding.tvApplications.text = job.applicationsCount.toString()
            binding.tvPostedDate.text = getTimeAgo(job.postedDate)
            binding.tvLocation.text = buildLocationString(job)

            // Status indicator
            val statusColor = if (job.isActive) R.color.success else R.color.text_hint
            binding.viewStatusBar.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )

            // Toggle switch (without triggering listener)
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = job.isActive
            binding.switchActive.setOnCheckedChangeListener { _, _ ->
                onToggleStatus(job)
            }

            binding.btnEdit.setOnClickListener { onEditClick(job) }
            binding.btnDelete.setOnClickListener { onDeleteClick(job) }
            binding.btnViewApplications.setOnClickListener { onViewApplications(job) }
        }
    }

    private fun buildLocationString(job: WorkData): String {
        val parts = mutableListOf<String>()
        if (job.city.isNotEmpty()) parts.add(job.city)
        if (job.state.isNotEmpty()) parts.add(job.state)
        if (job.pincode.isNotEmpty()) parts.add(job.pincode)
        return parts.joinToString(", ").ifEmpty { "No location" }
    }

    private fun getTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < TimeUnit.HOURS.toMillis(1) -> "Just now"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJobManageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount() = jobs.size

    fun updateData(newJobs: List<WorkData>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = jobs.size
            override fun getNewListSize() = newJobs.size
            override fun areItemsTheSame(old: Int, new: Int) = jobs[old].id == newJobs[new].id
            override fun areContentsTheSame(old: Int, new: Int) = jobs[old] == newJobs[new]
        })
        jobs = newJobs
        diffResult.dispatchUpdatesTo(this)
    }
}