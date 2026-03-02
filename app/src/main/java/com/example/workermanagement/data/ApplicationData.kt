package com.example.workermanagement.data

data class ApplicationData(
    var id: String = "",
    var jobId: String = "",
    var jobTitle: String = "",
    var workerId: String = "",
    var workerName: String = "",
    var workerPhone: String = "",
    var workerSkills: String = "",
    var workerExperienceYears: Int = 0,
    var managerId: String = "",
    var organizationName: String = "",
    var appliedDate: Long = 0L,
    var status: String = "pending",
    var workerNote: String = "",
    var managerNote: String = "",
    var reviewedDate: Long = 0L
)