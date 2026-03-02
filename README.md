# 👷 Worker Management App

> An industrial-strength Android platform connecting daily-wage workers with job opportunities across India.
> Apk debug and Apk release files present in root directory

---
Screenshots -
<img width="1063" height="2070" alt="1" src="https://github.com/user-attachments/assets/ce864d03-72df-4f04-a78c-ac032a603216" />
<img width="1063" height="2070" alt="2" src="https://github.com/user-attachments/assets/8ee758ce-6df8-4621-8052-d267565f7427" />
<img width="1063" height="2070" alt="3" src="https://github.com/user-attachments/assets/299e5859-8b55-4105-9c50-2a7a897fde2c" />
<img width="1063" height="2070" alt="4" src="https://github.com/user-attachments/assets/5001c76e-21f8-4279-a216-e0a698744041" />
<img width="1063" height="2070" alt="5" src="https://github.com/user-attachments/assets/cb1336b5-a60e-431b-9dde-963f8435b5a1" />
<img width="1063" height="2070" alt="6" src="https://github.com/user-attachments/assets/e0187103-b95c-4772-bea1-7acb3200438b" />
<img width="1063" height="2070" alt="7" src="https://github.com/user-attachments/assets/7db06934-6d36-4405-8f7f-8957c340937f" />
<img width="1063" height="2070" alt="8" src="https://github.com/user-attachments/assets/172ca964-fae6-4337-9e4e-1f39e73c6a57" />
<img width="1063" height="2070" alt="9" src="https://github.com/user-attachments/assets/e9085548-6c37-402d-a836-16cca8af2271" />


## 📋 Table of Contents

- [Overview](#overview)
- [Screenshots & Screens](#screens)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Firebase Setup](#firebase-setup)
- [Build & Run](#build--run)
- [Known Issues & Fixes](#known-issues--fixes)
- [Security](#security)

---

## Overview

Worker Management is a two-sided Android marketplace for India's informal labor sector. **Workers** browse and apply for wage jobs. **Managers (Employers)** post job listings with rich details — location, wages, benefits, requirements — and manage applications in real time.

Built with **Kotlin**, **Material Design 3**, and **Firebase Realtime Database**.

---

## Screens

| Role | Screen | Purpose |
|------|--------|---------|
| Shared | Main / Role Select | Choose Worker or Manager |
| Worker | Register | Create account with skills, location, Aadhar |
| Worker | Login | Authenticate |
| Worker | Job Search | Browse, search, filter live job listings |
| Worker | Job Detail | Full job info + one-tap apply |
| Worker | My Applications | Track application statuses |
| Worker | Profile | View and edit personal profile |
| Manager | Register | Create employer account with org details |
| Manager | Login | Authenticate |
| Manager | Dashboard | Stats + all posted jobs |
| Manager | Post / Edit Job | 7-section comprehensive job form |
| Manager | Applicants | Review, accept, or reject applications per job |

---

## Features

### For Workers
- Registration with skills, experience, location, Aadhar, bank details
- Real-time job search with 8 category filter chips (Construction, Farming, Driving, etc.)
- Advanced filters: city/state, wage type
- Full-text search across title, category, org name, skills, description
- Job detail view: wage, schedule, benefits, requirements, positions progress bar
- One-tap job application with duplicate prevention
- Application status tracking (Pending / Accepted / Rejected / Withdrawn)
- Editable profile with Firebase sync
- Session persistence (auto-login on reopen)

### For Managers
- Organization registration (GST, registration number, org type)
- Dashboard with live stats: active jobs, positions filled, total applications
- 35+ field job posting form across 7 sections:
  - Job Basics (title, category, description, urgent flag)
  - Location (address, city, state, pincode)
  - Compensation (wage, wage type, accommodation/meals/transport checkboxes)
  - Schedule (start/end dates, duration type, working hours/days)
  - Requirements (skills, age range, experience, gender, language, physical req, tools)
  - Positions (total, already filled)
  - Contact (phone, email, contact person)
- Edit and delete job postings
- Toggle job Active / Paused
- Per-job applicant list with Accept / Reject + optional reason note
- Real-time application count stats

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI Framework | Android SDK (minSdk 26, targetSdk 35) |
| Design System | Material Design 3 |
| View Binding | Enabled throughout |
| Backend | Firebase Realtime Database |
| Offline Support | Firebase persistence enabled |
| Image Loading | Glide 4.16.0 |
| RecyclerView | DiffUtil for smooth updates |
| Build System | Gradle (Kotlin DSL) |

---

## Architecture

```
Presentation Layer  →  Activities + Adapters  (UI)
        ↓
Repository Layer    →  FirebaseRepository     (single source of truth)
        ↓
Data Layer          →  Data Classes           (WorkData, WorkerData, ManagerData, ApplicationData)
        ↓
Backend             →  Firebase Realtime DB   (cloud persistence)
```

**Pattern:** Repository pattern with callback-based async. All Firebase operations are centralized in `FirebaseRepository.kt` — no Activity touches the database directly.

---

## Project Structure

```
app/src/main/
│
├── kotlin+java/com.example.workermanagement/
│   │
│   ├── data/
│   │   ├── WorkData.kt              # Job posting model (35+ fields)
│   │   ├── WorkerData.kt            # Worker profile model
│   │   ├── ManagerData.kt           # Employer profile model
│   │   └── ApplicationData.kt      # Job application lifecycle model
│   │
│   ├── repository/
│   │   └── FirebaseRepository.kt   # All Firebase CRUD + auth + session keys
│   │
│   ├── adapter/
│   │   ├── JobSearchAdapter.kt     # Worker job list (DiffUtil)
│   │   └── JobManageAdapter.kt     # Manager job list (DiffUtil)
│   │
│   └── ui/
│       ├── MainActivity.kt
│       ├── worker/
│       │   ├── LoginWorkerActivity.kt
│       │   ├── RegisterWorkerActivity.kt
│       │   ├── SearchActivity.kt
│       │   ├── JobDetailActivity.kt
│       │   ├── WorkerProfileActivity.kt
│       │   └── MyApplicationsActivity.kt
│       └── manager/
│           ├── LoginManagerActivity.kt
│           ├── RegisterManagerActivity.kt
│           ├── ManageActivity.kt
│           ├── PostWorkActivity.kt
│           └── ApplicationsActivity.kt
│
└── res/
    ├── layout/          # 13 XML layout files
    ├── drawable/        # 37 vector icons + shape backgrounds
    ├── values/          # colors.xml, themes.xml, strings.xml, dimens.xml
    └── anim/            # fade_slide_up.xml entrance animation
```

---

## Firebase Setup

### 1. Create Project
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create project → Add Android app → package: `com.example.workermanagement`
3. Download `google-services.json` → place in `app/` directory

### 2. Enable Realtime Database
1. Build → Realtime Database → Create database
2. Choose region: **asia-south1** (Mumbai) for India
3. Start in **test mode**

### 3. Database Rules (Development)
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

### 4. Database Indexes (for query performance)
```json
{
  "rules": {
    "workers":      { ".indexOn": ["username"] },
    "managers":     { ".indexOn": ["username"] },
    "jobs":         { ".indexOn": ["managerId", "postedDate"] },
    "applications": { ".indexOn": ["workerId", "jobId"] }
  }
}
```

### 5. Important: Regional URL
If your DB is in Singapore (`asia-southeast1`), set the URL explicitly in `FirebaseRepository.kt`:
```kotlin
FirebaseDatabase.getInstance(
    "https://YOUR-PROJECT-default-rtdb.asia-southeast1.firebasedatabase.app"
)
```
The correct URL is shown in the Firebase Console under Realtime Database → Data.

---

## Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11
- `google-services.json` in `app/`

### Steps
```bash
# Clone or open project in Android Studio
# Sync Gradle (File → Sync Project with Gradle Files)
# Run on emulator or device (API 26+)
```

### Test Accounts (after setup)
Create fresh accounts via the app's registration flows. Delete any test entries from Firebase Console before re-registering with the same username.

---

## Known Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| Login fails — `Snapshot exists: false` | Wrong database URL hardcoded | Use correct regional URL from Firebase Console |
| Jobs not showing in search | `orderByChild("isActive")` requires Firebase index | Fixed: client-side filter on full job fetch |
| `active` vs `isActive` mismatch | Kotlin strips `is` prefix on Boolean getters | Fixed: client-side filtering avoids index query |
| `Unresolved reference: jetbrains` | `build.gradle.kts` alias doesn't match `libs.versions.toml` key | Change `jetbrains.kotlin.android` → `kotlin.android` |
| `Cannot resolve @dimen/screen_min_height` | Missing `dimens.xml` | Add `res/values/dimens.xml` |

---

## Security

> ⚠️ This app currently stores plain-text passwords. Before production:

- [ ] Implement Firebase Authentication (email/password or phone OTP)
- [ ] Hash passwords with BCrypt / PBKDF2
- [ ] Set restrictive Firebase Rules (auth-gated read/write)
- [ ] Enable Firebase App Check
- [ ] Encrypt sensitive fields (Aadhar, bank account numbers)
- [ ] Add server-side validation via Firebase Functions
- [ ] Remove `debuggable = true` from release build
- [ ] Enable ProGuard / R8 for release

---

## License

This project is for educational and development purposes.

---

*Built with ❤️ for India's daily wage workers*
