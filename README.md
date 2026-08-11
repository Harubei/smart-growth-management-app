# Smart Growth Tutorial Center App 🚀

An enterprise-grade, offline-first Android application designed to manage operations for a fast-paced tutorial center in the Philippines. 

## 🛠 Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Modern Declarative UI)
* **Local Database:** Room (SQLite) for zero-latency offline operations
* **Cloud Sync:** Firebase Firestore (NoSQL) for background data backup
* **Architecture:** MVVM (Model-View-ViewModel) with Kotlin Coroutines & Flows

## ✨ Key Features (Beta v1.0)
* **Offline-First:** Receptionists can book classes and process payments seamlessly even when the center's internet drops.
* **Sync Engine:** A custom background Mutex-locked engine that pushes/pulls data to Firebase when connectivity is restored.
* **Dynamic Availability:** Tutors have custom daily schedules to prevent double-booking.
* **Enterprise Dashboard:** Real-time calculation of daily revenue, active students, and ongoing sessions.

## 📖 Production Journey
* **Phase 1:** Migrated from a legacy PHP/MySQL web app to a localized Android architecture to eliminate monthly server hosting costs.
* **Phase 2:** Upgraded to an offline-first architecture using Room and Firebase to solve intermittent internet issues at the physical center location.
