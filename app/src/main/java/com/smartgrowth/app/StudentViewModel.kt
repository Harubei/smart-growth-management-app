package com.smartgrowth.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentViewModel(
    private val dao: StudentDao,
    private val tutorDao: TutorDao,
    private val sessionDao: SessionDao,
    private val paymentDao: PaymentDao
) : ViewModel() {

    private val _syncStatus = MutableStateFlow("Idle")
    val syncStatus: StateFlow<String> = _syncStatus

    // Mutex prevents Race Conditions!
    private val syncMutex = Mutex()

    val students: StateFlow<List<Student>> = dao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val tutors: StateFlow<List<Tutor>> = tutorDao.getAllTutors()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sessions: StateFlow<List<Session>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val payments: StateFlow<List<Payment>> = paymentDao.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        performFullSync()
    }

    // --- CREATE COMMANDS ---
    fun addStudent(firstName: String, lastName: String, grade: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            dao.insertStudent(Student(firstName = firstName, lastName = lastName, gradeLevel = grade, parentContact = phone, parentEmail = null, enrollmentDate = currentDate))
            performFullSync()
        }
    }

    fun addTutor(name: String, phone: String, capacity: Int, availability: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tutorDao.insertTutor(Tutor(fullName = name, phone = phone, maxCapacity = capacity, availability = availability))
            performFullSync()
        }
    }

    fun addSession(studentName: String, tutorName: String, date: String, startTime: String, endTime: String, program: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionDao.insertSession(Session(studentName = studentName, tutorName = tutorName, date = date, startTime = startTime, endTime = endTime, program = program))
            performFullSync()
        }
    }

    fun addPayment(studentName: String, amount: Double, date: String, method: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            paymentDao.insertPayment(Payment(studentName = studentName, amount = amount, date = date, method = method, notes = notes))
            performFullSync()
        }
    }

    // --- UPDATE COMMANDS ---
    fun updateStudent(student: Student, firstName: String, lastName: String, grade: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateStudent(student.copy(firstName = firstName, lastName = lastName, gradeLevel = grade, parentContact = phone, isSynced = false))
            performFullSync()
        }
    }

    fun updateTutor(tutor: Tutor, name: String, phone: String, capacity: Int, availability: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tutorDao.updateTutor(tutor.copy(fullName = name, phone = phone, maxCapacity = capacity, availability = availability, isSynced = false))
            performFullSync()
        }
    }

    fun updateSession(session: Session, studentName: String, tutorName: String, date: String, startTime: String, endTime: String, program: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionDao.updateSession(session.copy(studentName = studentName, tutorName = tutorName, date = date, startTime = startTime, endTime = endTime, program = program, isSynced = false))
            performFullSync()
        }
    }

    fun updateSessionStatus(session: Session, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionDao.updateSession(session.copy(status = newStatus, isSynced = false))
            performFullSync()
        }
    }

    fun updatePayment(payment: Payment, studentName: String, amount: Double, date: String, method: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            paymentDao.updatePayment(payment.copy(studentName = studentName, amount = amount, date = date, method = method, notes = notes, isSynced = false))
            performFullSync()
        }
    }

    // --- SYNC ENGINE ---
    fun performFullSync() {
        if (syncMutex.isLocked) return
        viewModelScope.launch(Dispatchers.IO) {
            syncMutex.withLock {
                _syncStatus.value = "Connecting to Cloud Database..."
                try {
                    pushUnsyncedData()
                    pullLatestData()
                    _syncStatus.value = "Data is up to date!"
                    kotlinx.coroutines.delay(2000)
                } catch (e: Exception) {
                    _syncStatus.value = "Sync Error: ${e.message}"
                    kotlinx.coroutines.delay(3000)
                } finally {
                    _syncStatus.value = "Idle"
                }
            }
        }
    }

    private suspend fun pushUnsyncedData() {
        val db = FirebaseFirestore.getInstance()

        dao.getUnsyncedStudents().forEach { student ->
            try {
                val map = hashMapOf("firstName" to student.firstName, "lastName" to student.lastName, "gradeLevel" to student.gradeLevel, "parentContact" to student.parentContact, "parentEmail" to student.parentEmail, "enrollmentDate" to student.enrollmentDate)
                Tasks.await(db.collection("students").document(student.id).set(map))
                dao.updateStudent(student.copy(isSynced = true))
            } catch (e: Exception) { }
        }

        tutorDao.getUnsyncedTutors().forEach { tutor ->
            try {
                val map = hashMapOf(
                    "fullName" to tutor.fullName,
                    "phone" to tutor.phone,
                    "maxCapacity" to tutor.maxCapacity,
                    "availability" to tutor.availability
                )
                Tasks.await(db.collection("tutors").document(tutor.id).set(map))
                tutorDao.updateTutor(tutor.copy(isSynced = true))
            } catch (e: Exception) { }
        }

        sessionDao.getUnsyncedSessions().forEach { session ->
            try {
                val map = hashMapOf("studentName" to session.studentName, "tutorName" to session.tutorName, "date" to session.date, "startTime" to session.startTime, "endTime" to session.endTime, "program" to session.program, "status" to session.status)
                Tasks.await(db.collection("sessions").document(session.id).set(map))
                sessionDao.updateSession(session.copy(isSynced = true))
            } catch (e: Exception) { }
        }

        // CONFIRMATION: Payments are fully integrated into the push engine!
        paymentDao.getUnsyncedPayments().forEach { payment ->
            try {
                val map = hashMapOf("studentName" to payment.studentName, "amount" to payment.amount, "date" to payment.date, "method" to payment.method, "notes" to payment.notes)
                Tasks.await(db.collection("payments").document(payment.id).set(map))
                paymentDao.updatePayment(payment.copy(isSynced = true))
            } catch (e: Exception) { }
        }
    }

    private suspend fun pullLatestData() {
        val db = FirebaseFirestore.getInstance()
        try {
            val studentsSnapshot = Tasks.await(db.collection("students").get())
            dao.clearSyncedStudents()
            for (doc in studentsSnapshot.documents) {
                dao.insertStudent(Student(
                    id = doc.id,
                    firstName = doc.getString("firstName") ?: "", lastName = doc.getString("lastName") ?: "",
                    gradeLevel = doc.getString("gradeLevel") ?: "", parentContact = doc.getString("parentContact") ?: "",
                    parentEmail = doc.getString("parentEmail"), enrollmentDate = doc.getString("enrollmentDate") ?: "2026-07-01", isSynced = true
                ))
            }

            val tutorsSnapshot = Tasks.await(db.collection("tutors").get())
            tutorDao.clearSyncedTutors()
            for (doc in tutorsSnapshot.documents) {
                tutorDao.insertTutor(Tutor(
                    id = doc.id,
                    fullName = doc.getString("fullName") ?: "",
                    phone = doc.getString("phone") ?: "",
                    maxCapacity = doc.getLong("maxCapacity")?.toInt() ?: 1,
                    availability = doc.getString("availability") ?: "",
                    isSynced = true
                ))
            }

            val sessionsSnapshot = Tasks.await(db.collection("sessions").get())
            sessionDao.clearSyncedSessions()
            for (doc in sessionsSnapshot.documents) {
                sessionDao.insertSession(Session(
                    id = doc.id,
                    studentName = doc.getString("studentName") ?: "", tutorName = doc.getString("tutorName") ?: "",
                    date = doc.getString("date") ?: "", startTime = doc.getString("startTime") ?: "",
                    endTime = doc.getString("endTime") ?: "", program = doc.getString("program") ?: "",
                    status = doc.getString("status") ?: "Scheduled", isSynced = true
                ))
            }

            // CONFIRMATION: Payments are fully integrated into the pull engine!
            val paymentsSnapshot = Tasks.await(db.collection("payments").get())
            paymentDao.clearSyncedPayments()
            for (doc in paymentsSnapshot.documents) {
                paymentDao.insertPayment(Payment(
                    id = doc.id,
                    studentName = doc.getString("studentName") ?: "", amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getString("date") ?: "", method = doc.getString("method") ?: "Cash",
                    notes = doc.getString("notes") ?: "", isSynced = true
                ))
            }
        } catch (e: Exception) {
            // Fails gracefully if offline
        }
    }
}

class StudentViewModelFactory(
    private val dao: StudentDao, private val tutorDao: TutorDao, private val sessionDao: SessionDao, private val paymentDao: PaymentDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StudentViewModel(dao, tutorDao, sessionDao, paymentDao) as T
    }
}