package com.example.workermanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.workermanagement.R
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ItemJobSearchBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class JobSearchAdapter(
    private var jobs: List<WorkData>,
    private val onJobClick: (WorkData) -> Unit
) : RecyclerView.Adapter<JobSearchAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemJobSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: WorkData) {
            binding.tvTitle.text = job.title
            binding.tvCategory.text = job.category
            binding.tvOrganization.text = job.organizationName.ifEmpty { job.managerName }
            binding.tvLocation.text = buildLocationString(job)
            binding.tvWage.text = "₹${job.wage}"
            binding.tvWageType.text = "per ${job.wageType.lowercase()}"
            binding.tvPositions.text = "${job.availablePositions} open"
            binding.tvPostedDate.text = getTimeAgo(job.postedDate)

            // Urgent badge
            binding.tvUrgent.visibility = if (job.isUrgent) View.VISIBLE else View.GONE

            // Benefits
            binding.tvAccommodation.visibility = if (job.accommodationProvided) View.VISIBLE else View.GONE
            binding.tvMeals.visibility = if (job.mealsProvided) View.VISIBLE else View.GONE
            binding.tvTransport.visibility = if (job.transportProvided) View.VISIBLE else View.GONE

            // Click listener
            binding.root.setOnClickListener { onJobClick(job) }
        }
    }

    private fun buildLocationString(job: WorkData): String {
        val parts = mutableListOf<String>()
        if (job.city.isNotEmpty()) parts.add(job.city)
        if (job.state.isNotEmpty()) parts.add(job.state)
        if (job.pincode.isNotEmpty()) parts.add(job.pincode)
        return parts.joinToString(", ").ifEmpty { "Location not specified" }
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
        val binding = ItemJobSearchBinding.inflate(
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