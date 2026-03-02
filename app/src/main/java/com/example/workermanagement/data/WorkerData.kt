package com.example.workermanagement.data

data class WorkerData(

    // Auth
    var id: String = "",
    var username: String = "",
    var password: String = "",

    // Personal Info
    var fullName: String = "",
    var phone: String = "",
    var email: String = "",
    var dateOfBirth: String = "",
    var gender: String = "",
    var profilePhotoUrl: String = "",

    // Identity
    var aadharNumber: String = "",
    var idProofType: String = "aadhar",
    var idProofNumber: String = "",

    // Location
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var pincode: String = "",

    // Work Profile
    var primarySkill: String = "",
    var skills: String = "",
    var experienceYears: Int = 0,
    var preferredCategory: String = "",
    var preferredWageType: String = "daily",
    var expectedWage: String = "",
    var willingToRelocate: Boolean = false,
    var availableFrom: String = "",

    // Documents
    var hasAadhar: Boolean = false,
    var hasPan: Boolean = false,
    var hasBankAccount: Boolean = false,
    var bankAccountNumber: String = "",
    var ifscCode: String = "",

    // Metadata (MATCH DB KEYS EXACTLY)
    var registrationDate: Long = 0L,
    var lastActive: Long = 0L,
    var verified: Boolean = false,   // ✅ renamed
    var active: Boolean = true,      // ✅ renamed
    var totalJobsCompleted: Int = 0,
    var rating: Int = 0,             // ✅ type fixed
    var reviewCount: Int = 0
)