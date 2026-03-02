package com.example.workermanagement.data

data class WorkData(
    var id: String = "",
    // Core Info
    var title: String = "",
    var category: String = "",          // Construction, Farming, Manufacturing, etc.
    var description: String = "",
    // Location
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var pincode: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    // Compensation
    var wage: String = "",
    var wageType: String = "daily",     // daily, weekly, monthly, fixed
    var currency: String = "INR",
    var accommodationProvided: Boolean = false,
    var mealsProvided: Boolean = false,
    var transportProvided: Boolean = false,
    // Duration & Schedule
    var startDate: String = "",
    var endDate: String = "",
    var durationType: String = "short_term",  // short_term, long_term, contract, seasonal
    var workingHours: String = "8",     // hours per day
    var workingDays: String = "Mon-Sat",
    // Requirements
    var skillsRequired: String = "",
    var experienceYears: Int = 0,
    var minAge: Int = 18,
    var maxAge: Int = 60,
    var genderPreference: String = "any",  // male, female, any
    var physicalRequirements: String = "",
    var languageRequired: String = "",
    var toolsProvided: Boolean = false,
    var ownToolsRequired: Boolean = false,
    // Positions
    var totalPositions: Int = 1,
    var filledPositions: Int = 0,
    // Contact
    var phone: String = "",
    var email: String = "",
    var contactPerson: String = "",
    // Manager Info
    var managerName: String = "",
    var managerId: String = "",
    var organizationName: String = "",
    // Metadata
    var postedDate: Long = 0L,
    var lastUpdated: Long = 0L,
    var isActive: Boolean = true,
    var views: Int = 0,
    var applicationsCount: Int = 0,
    var isUrgent: Boolean = false,
    var isFeatured: Boolean = false
) {
    val availablePositions: Int get() = totalPositions - filledPositions
    val isFullyFilled: Boolean get() = filledPositions >= totalPositions
}