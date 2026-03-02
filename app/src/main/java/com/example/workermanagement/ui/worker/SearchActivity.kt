package com.example.workermanagement

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workermanagement.adapter.JobSearchAdapter
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.databinding.ActivitySearchBinding
import com.example.workermanagement.repository.FirebaseRepository
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: JobSearchAdapter
    private var allJobs: List<WorkData> = emptyList()
    private var dbListener: ValueEventListener? = null

    // Filter state
    private var selectedCategory: String? = null
    private var filterCity: String? = null
    private var filterWageType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        setupFilterPanel()
        setupNavButtons()
        loadJobs()
    }

    private fun setupRecyclerView() {
        adapter = JobSearchAdapter(emptyList()) { job ->
            startActivity(Intent(this, JobDetailActivity::class.java).apply {
                putExtra("jobId", job.id)
            })
            FirebaseRepository.incrementJobViews(job.id)
        }
        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupCategoryChips() {
        // "All" chip resets category
        binding.chipAll.setOnClickListener       { selectCategory(null) }
        binding.chipConstruction.setOnClickListener { selectCategory("Construction") }
        binding.chipFarming.setOnClickListener      { selectCategory("Farming") }
        binding.chipManufacturing.setOnClickListener{ selectCategory("Manufacturing") }
        binding.chipDriving.setOnClickListener      { selectCategory("Driving") }
        binding.chipCleaning.setOnClickListener     { selectCategory("Cleaning") }
        binding.chipSecurity.setOnClickListener     { selectCategory("Security") }
        binding.chipOther.setOnClickListener        { selectCategory("Other") }

        // Start with "All" selected
        binding.chipAll.isChecked = true
    }

    private fun selectCategory(category: String?) {
        // Toggle off if already selected
        selectedCategory = if (selectedCategory == category) null else category
        refreshChipStates()
        applyFilters()
    }

    private fun refreshChipStates() {
        binding.chipAll.isChecked          = selectedCategory == null
        binding.chipConstruction.isChecked = selectedCategory == "Construction"
        binding.chipFarming.isChecked      = selectedCategory == "Farming"
        binding.chipManufacturing.isChecked= selectedCategory == "Manufacturing"
        binding.chipDriving.isChecked      = selectedCategory == "Driving"
        binding.chipCleaning.isChecked     = selectedCategory == "Cleaning"
        binding.chipSecurity.isChecked     = selectedCategory == "Security"
        binding.chipOther.isChecked        = selectedCategory == "Other"
    }

    private fun setupFilterPanel() {
        // Wage type dropdown
        val wageTypes = arrayOf("Any", "Daily", "Weekly", "Monthly", "Fixed Contract")
        binding.actvWageFilter.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, wageTypes)
        )

        binding.btnFilterPanel.setOnClickListener {
            binding.filterPanel.visibility =
                if (binding.filterPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        binding.btnApplyFilters.setOnClickListener {
            filterCity     = binding.etFilterCity.text.toString().trim().ifEmpty { null }
            val wt         = binding.actvWageFilter.text.toString().trim().lowercase()
            filterWageType = if (wt.isEmpty() || wt == "any") null else wt
            binding.filterPanel.visibility = View.GONE
            applyFilters()
        }

        binding.btnClearFilters.setOnClickListener {
            filterCity     = null
            filterWageType = null
            selectedCategory = null
            binding.etSearch.setText("")
            binding.etFilterCity.setText("")
            binding.actvWageFilter.setText("")
            refreshChipStates()
            applyFilters()
        }
    }

    private fun setupNavButtons() {
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, WorkerProfileActivity::class.java))
        }
        binding.btnApplications.setOnClickListener {
            startActivity(Intent(this, MyApplicationsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        val filtered = allJobs.filter { job ->

            // Text search: title, category, city, org name, skills, description
            val matchesSearch = query.isEmpty() ||
                    job.title.lowercase().contains(query) ||
                    job.category.lowercase().contains(query) ||
                    job.city.lowercase().contains(query) ||
                    job.organizationName.lowercase().contains(query) ||
                    job.skillsRequired.lowercase().contains(query) ||
                    job.description.lowercase().contains(query) ||
                    job.managerName.lowercase().contains(query)

            // Category chip filter
            val matchesCategory = selectedCategory == null ||
                    job.category.equals(selectedCategory, ignoreCase = true)

            // City/state filter (matches either field)
            val matchesCity = filterCity == null ||
                    job.city.lowercase().contains(filterCity!!.lowercase()) ||
                    job.state.lowercase().contains(filterCity!!.lowercase())

            // Wage type filter
            val matchesWage = filterWageType == null ||
                    job.wageType.lowercase() == filterWageType!!.lowercase()

            matchesSearch && matchesCategory && matchesCity && matchesWage
        }

        Log.d("WM_SEARCH", "Filter results: ${filtered.size} / ${allJobs.size} total")

        adapter.updateData(filtered)
        binding.tvResultCount.text = "${filtered.size} job${if (filtered.size != 1) "s" else ""} found"
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvJobs.visibility      = if (filtered.isEmpty()) View.GONE  else View.VISIBLE
    }

    private fun loadJobs() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility     = View.GONE

        dbListener = FirebaseRepository.listenAllActiveJobs(
            onUpdate = { jobs ->
                binding.progressBar.visibility = View.GONE
                Log.d("WM_SEARCH", "Jobs received: ${jobs.size}")
                allJobs = jobs
                applyFilters()
            },
            onError = { msg ->
                binding.progressBar.visibility = View.GONE
                binding.tvError.text = "⚠️ $msg"
                binding.tvError.visibility = View.VISIBLE
                Log.e("WM_SEARCH", "Error loading jobs: $msg")
            }
        )
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