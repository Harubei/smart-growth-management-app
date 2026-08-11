package com.smartgrowth.app

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Safely escapes commas and quotes so Excel reads the data perfectly
private fun String.toCSV(): String {
    return "\"${this.replace("\"", "\"\"")}\""
}

fun exportDataToCSV(
    context: Context,
    students: List<Student>,
    tutors: List<Tutor>,
    sessions: List<Session>,
    payments: List<Payment>
) {
    try {
        val currentDate = Date()
        // Format for the main ZIP file (includes time to prevent overwriting) e.g., 2026-08-11_2015
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(currentDate)
        // Format for the inner CSV files (just the date for cleanliness) e.g., 2026-08-11
        val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create the single ZIP file with the timestamped name
        val zipFile = File(downloadsDir, "SmartGrowth_Export_$timestamp.zip")
        val zipOut = ZipOutputStream(FileOutputStream(zipFile))

        // Helper function to create a new "file" inside the ZIP
        fun addCsvToZip(filename: String, header: String, rows: List<String>) {
            zipOut.putNextEntry(ZipEntry(filename))
            zipOut.write("$header\n".toByteArray())
            rows.forEach { zipOut.write("$it\n".toByteArray()) }
            zipOut.closeEntry()
        }

        // 1. STUDENTS
        val studentRows = students.map { "${it.id},${it.firstName.toCSV()},${it.middleName.toCSV()},${it.lastName.toCSV()},${it.gradeLevel.toCSV()},${it.parentContact.toCSV()},${it.parentEmail.toCSV()},${it.schoolEnrolled.toCSV()},${it.enrollmentDate}" }
        addCsvToZip("1_Students_$dateStamp.csv", "ID,First Name,Middle Name,Last Name,Grade,Parent Contact,Email,School,Enrollment Date", studentRows)

        // 2. TUTORS
        val tutorRows = tutors.map { "${it.id},${it.firstName.toCSV()},${it.middleName.toCSV()},${it.lastName.toCSV()},${it.phone.toCSV()},${it.email.toCSV()},${it.hireDate.toCSV()},${it.maxCapacity},${it.subjectsHandled.toCSV()},${it.availability.toCSV()}" }
        addCsvToZip("2_Staff_and_Tutors_$dateStamp.csv", "ID,First Name,Middle Name,Last Name,Phone,Email,Hire Date,Capacity,Subjects,Availability", tutorRows)

        // 3. SESSIONS
        val sessionRows = sessions.map { "${it.id},${it.date},${it.startTime},${it.endTime},${it.studentName.toCSV()},${it.tutorName.toCSV()},${it.program.toCSV()},${it.status}" }
        addCsvToZip("3_Sessions_$dateStamp.csv", "ID,Date,Start Time,End Time,Student,Tutor,Program,Status", sessionRows)

        // 4. PAYMENTS
        val paymentRows = payments.map { "${it.id},${it.date},${it.studentName.toCSV()},${it.amount},${it.method},${it.notes.toCSV()}" }
        addCsvToZip("4_Payments_$dateStamp.csv", "ID,Date,Student,Amount,Method,Notes", paymentRows)

        // Close and save the ZIP
        zipOut.close()

        Toast.makeText(context, "Success! Exported to Downloads/${zipFile.name}", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Export Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}