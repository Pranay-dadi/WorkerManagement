package com.example.workermanagement.data

data class ManagerData(

    // Auth
    var id: String = "",
    var username: String = "",
    var password: String = "",

    // Personal Info
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var dateOfBirth: String = "",
    var profilePhotoUrl: String = "",

    // Organization
    var organizationName: String = "",
    var organizationType: String = "",
    var organizationAddress: String = "",
    var organizationCity: String = "",
    var organizationState: String = "",
    var organizationPincode: String = "",
    var gstNumber: String = "",
    var registrationNumber: String = "",

    // Contact
    var alternatePhone: String = "",
    var website: String = "",

    // Metadata (MATCH DB KEYS)
    var registrationDate: Long = 0L,
    var verified: Boolean = false,   // 🔁 renamed
    var active: Boolean = true,      // 🔁 renamed
    var totalJobsPosted: Int = 0,
    var rating: Int = 0,             // 🔁 safer than Float
    var reviewCount: Int = 0
)