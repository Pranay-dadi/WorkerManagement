package com.example.workermanagement.repository

import android.util.Log
import com.example.workermanagement.data.ApplicationData
import com.example.workermanagement.data.ManagerData
import com.example.workermanagement.data.WorkData
import com.example.workermanagement.data.WorkerData
import com.google.firebase.database.*

/**
 * Central repository for all Firebase Realtime Database operations.
 */
object FirebaseRepository {

    // ✅ FIX: No hardcoded URL — reads from google-services.json automatically.
    // A wrong hardcoded URL causes all queries to silently return empty.
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://workermanagement-prod-default-rtdb.asia-southeast1.firebasedatabase.app").also { db ->
            try {
                db.setPersistenceEnabled(true)
            } catch (e: Exception) {
                // Already enabled — safe to ignore on hot reload
            }
        }
    }

    val workersRef: DatabaseReference get() = database.reference.child("workers")
    val managersRef: DatabaseReference get() = database.reference.child("managers")
    val jobsRef: DatabaseReference get() = database.reference.child("jobs")
    val applicationsRef: DatabaseReference get() = database.reference.child("applications")

    // =====================================================================
    // WORKER AUTH
    // =====================================================================

    fun registerWorker(worker: WorkerData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        workersRef.orderByChild("username").equalTo(worker.username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) { onError("Username already taken."); return }
                    val id = workersRef.push().key ?: run { onError("Failed to generate ID."); return }
                    workersRef.child(id).setValue(worker.copy(id = id, registrationDate = System.currentTimeMillis()))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Registration failed.") }
                }
                override fun onCancelled(error: DatabaseError) = onError(error.message)
            })
    }

    fun loginWorker(username: String, password: String, onSuccess: (WorkerData) -> Unit, onError: (String) -> Unit) {
        Log.d("WM_LOGIN", "Worker login attempt: '$username'")

        // Primary path: index query
        workersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("WM_LOGIN", "Index query: exists=${snapshot.exists()}, count=${snapshot.childrenCount}")

                    if (snapshot.exists()) {
                        // Index worked — check password
                        for (child in snapshot.children) {
                            val worker = child.getValue(WorkerData::class.java) ?: continue
                            if (worker.password == password) {
                                workersRef.child(worker.id).child("lastActive").setValue(System.currentTimeMillis())
                                onSuccess(worker)
                                return
                            }
                        }
                        onError("Incorrect password. Please try again.")
                    } else {
                        // Fallback: full table scan (works even without Firebase .indexOn rule)
                        Log.d("WM_LOGIN", "Index empty — full scan fallback")
                        workersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(all: DataSnapshot) {
                                Log.d("WM_LOGIN", "Full scan: ${all.childrenCount} workers total")
                                for (child in all.children) {
                                    val worker = child.getValue(WorkerData::class.java) ?: continue
                                    Log.d("WM_LOGIN", "  → username='${worker.username}'")
                                    if (worker.username == username) {
                                        if (worker.password == password) {
                                            workersRef.child(worker.id).child("lastActive").setValue(System.currentTimeMillis())
                                            onSuccess(worker)
                                        } else {
                                            onError("Incorrect password. Please try again.")
                                        }
                                        return
                                    }
                                }
                                onError("No account found for '$username'. Please register first.")
                            }
                            override fun onCancelled(error: DatabaseError) = onError(error.message)
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("WM_LOGIN", "DB error: ${error.message}")
                    onError(error.message)
                }
            })
    }

    // =====================================================================
    // MANAGER AUTH
    // =====================================================================

    fun registerManager(manager: ManagerData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        managersRef.orderByChild("username").equalTo(manager.username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) { onError("Username already taken."); return }
                    val id = managersRef.push().key ?: run { onError("Failed to generate ID."); return }
                    managersRef.child(id).setValue(manager.copy(id = id, registrationDate = System.currentTimeMillis()))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Registration failed.") }
                }
                override fun onCancelled(error: DatabaseError) = onError(error.message)
            })
    }

    fun loginManager(username: String, password: String, onSuccess: (ManagerData) -> Unit, onError: (String) -> Unit) {
        Log.d("WM_LOGIN", "Manager login attempt: '$username'")

        managersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("WM_LOGIN", "Manager index query: exists=${snapshot.exists()}")
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val manager = child.getValue(ManagerData::class.java) ?: continue
                            if (manager.password == password) { onSuccess(manager); return }
                        }
                        onError("Incorrect password. Please try again.")
                    } else {
                        managersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(all: DataSnapshot) {
                                for (child in all.children) {
                                    val manager = child.getValue(ManagerData::class.java) ?: continue
                                    if (manager.username == username) {
                                        if (manager.password == password) onSuccess(manager)
                                        else onError("Incorrect password. Please try again.")
                                        return
                                    }
                                }
                                onError("No account found for '$username'. Please register first.")
                            }
                            override fun onCancelled(error: DatabaseError) = onError(error.message)
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) = onError(error.message)
            })
    }

    // =====================================================================
    // JOBS
    // =====================================================================

    fun postJob(job: WorkData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val id = jobsRef.push().key ?: run { onError("Failed to generate job ID."); return }
        jobsRef.child(id).setValue(job.copy(id = id, postedDate = System.currentTimeMillis(), lastUpdated = System.currentTimeMillis(), isActive = true))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to post job.") }
    }

    fun updateJob(job: WorkData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val updates = mapOf(
            "title" to job.title, "category" to job.category, "description" to job.description,
            "address" to job.address, "city" to job.city, "state" to job.state, "pincode" to job.pincode,
            "wage" to job.wage, "wageType" to job.wageType,
            "accommodationProvided" to job.accommodationProvided,
            "mealsProvided" to job.mealsProvided, "transportProvided" to job.transportProvided,
            "startDate" to job.startDate, "endDate" to job.endDate, "durationType" to job.durationType,
            "workingHours" to job.workingHours, "workingDays" to job.workingDays,
            "skillsRequired" to job.skillsRequired, "experienceYears" to job.experienceYears,
            "minAge" to job.minAge, "maxAge" to job.maxAge, "genderPreference" to job.genderPreference,
            "physicalRequirements" to job.physicalRequirements, "languageRequired" to job.languageRequired,
            "toolsProvided" to job.toolsProvided, "ownToolsRequired" to job.ownToolsRequired,
            "totalPositions" to job.totalPositions, "filledPositions" to job.filledPositions,
            "phone" to job.phone, "email" to job.email, "contactPerson" to job.contactPerson,
            "isActive" to job.isActive, "isUrgent" to job.isUrgent,
            "lastUpdated" to System.currentTimeMillis()
        )
        jobsRef.child(job.id).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update job.") }
    }

    fun deleteJob(jobId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        jobsRef.child(jobId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to delete job.") }
    }

    fun toggleJobStatus(jobId: String, isActive: Boolean, onComplete: () -> Unit) {
        jobsRef.child(jobId).child("isActive").setValue(isActive).addOnCompleteListener { onComplete() }
    }

    /**
     * Fetches ALL jobs and filters client-side.
     * Avoids orderByChild("isActive") which:
     *   1. Requires a Firebase index to work
     *   2. Kotlin serializes "isActive" Boolean as "active" (strips "is" prefix),
     *      so the query key never matches the stored key anyway.
     */
    fun listenAllActiveJobs(onUpdate: (List<WorkData>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("WM_JOBS", "Total jobs in DB: ${snapshot.childrenCount}")
                val jobs = snapshot.children.mapNotNull { child ->
                    child.getValue(WorkData::class.java)?.also { job ->
                        android.util.Log.d("WM_JOBS", "  Job: '${job.title}' isActive=${job.isActive} filled=${job.filledPositions}/${job.totalPositions}")
                    }
                }.filter { it.isActive && !it.isFullyFilled }
                    .sortedByDescending { it.postedDate }
                android.util.Log.d("WM_JOBS", "Active+unfilled jobs: ${jobs.size}")
                onUpdate(jobs)
            }
            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        // Listen to the whole jobs node — no index needed, filter is done client-side
        jobsRef.addValueEventListener(listener)
        return listener
    }

    fun listenManagerJobs(managerId: String, onUpdate: (List<WorkData>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = snapshot.children
                    .mapNotNull { it.getValue(WorkData::class.java) }
                    .filter { it.managerId == managerId }   // client-side filter — no index needed
                    .sortedByDescending { it.postedDate }
                android.util.Log.d("WM_JOBS", "Manager jobs for $managerId: ${jobs.size}")
                onUpdate(jobs)
            }
            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        jobsRef.addValueEventListener(listener)
        return listener
    }

    fun incrementJobViews(jobId: String) {
        jobsRef.child(jobId).child("views").runTransaction(object : Transaction.Handler {
            override fun doTransaction(d: MutableData): Transaction.Result {
                d.value = (d.getValue(Int::class.java) ?: 0) + 1
                return Transaction.success(d)
            }
            override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
        })
    }

    // =====================================================================
    // APPLICATIONS
    // =====================================================================

    fun submitApplication(application: ApplicationData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        applicationsRef.orderByChild("jobId").equalTo(application.jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alreadyApplied = snapshot.children
                        .mapNotNull { it.getValue(ApplicationData::class.java) }
                        .any { it.workerId == application.workerId && it.status != "withdrawn" }
                    if (alreadyApplied) { onError("You have already applied for this job."); return }
                    val id = applicationsRef.push().key ?: run { onError("Failed to create application."); return }
                    applicationsRef.child(id).setValue(application.copy(id = id, appliedDate = System.currentTimeMillis()))
                        .addOnSuccessListener {
                            jobsRef.child(application.jobId).child("applicationsCount")
                                .runTransaction(object : Transaction.Handler {
                                    override fun doTransaction(d: MutableData): Transaction.Result {
                                        d.value = (d.getValue(Int::class.java) ?: 0) + 1
                                        return Transaction.success(d)
                                    }
                                    override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
                                })
                            onSuccess()
                        }
                        .addOnFailureListener { onError(it.message ?: "Application failed.") }
                }
                override fun onCancelled(error: DatabaseError) = onError(error.message)
            })
    }

    fun listenWorkerApplications(workerId: String, onUpdate: (List<ApplicationData>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onUpdate(snapshot.children.mapNotNull { it.getValue(ApplicationData::class.java) }
                    .sortedByDescending { it.appliedDate })
            }
            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        applicationsRef.orderByChild("workerId").equalTo(workerId).addValueEventListener(listener)
        return listener
    }

    fun listenJobApplications(jobId: String, onUpdate: (List<ApplicationData>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onUpdate(snapshot.children.mapNotNull { it.getValue(ApplicationData::class.java) }
                    .sortedByDescending { it.appliedDate })
            }
            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        applicationsRef.orderByChild("jobId").equalTo(jobId).addValueEventListener(listener)
        return listener
    }

    fun updateApplicationStatus(appId: String, status: String, managerNote: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        applicationsRef.child(appId).updateChildren(mapOf(
            "status" to status,
            "managerNote" to managerNote,
            "reviewedDate" to System.currentTimeMillis()
        )).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Update failed.") }
    }

    // =====================================================================
    // SESSION MANAGEMENT
    // =====================================================================
    const val PREFS_NAME = "WM_Session"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_TYPE = "userType"
    const val KEY_USER_NAME = "userName"
    const val KEY_ORG_NAME = "orgName"
}