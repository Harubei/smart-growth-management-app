package com.smartgrowth.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val BrandBlue = Color(0xFF1E3A8A)
val BrandLightBlue = Color(0xFF3B82F6)
val BrandGreen = Color(0xFF84CC16)
val NotebookBackground = Color(0xFFF0F4F8)
val ChalkGray = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(viewModel: StudentViewModel) {
    val students by viewModel.students.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = "School Logo", modifier = Modifier.padding(end = 8.dp))
                        Text("Student Desk", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.performFullSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync to Cloud", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlue, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Enroll")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enroll", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(NotebookBackground)) {

            if (syncStatus != "Idle") {
                Surface(color = BrandLightBlue, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = syncStatus, modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelMedium, color = Color.White,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (students.isEmpty()) {
                    item { EmptyClassroomState(onEnrollClick = { showAddDialog = true }) }
                } else {
                    items(students) { student ->
                        StudentFlashcard(student = student, onClick = { editingStudent = student })
                    }
                }
            }
        }

        if (showAddDialog || editingStudent != null) {
            EnrollmentFormDialog(
                student = editingStudent,
                onDismiss = {
                    showAddDialog = false
                    editingStudent = null
                },
                onSave = { firstName, lastName, grade, phone ->
                    if (editingStudent != null) {
                        viewModel.updateStudent(editingStudent!!, firstName, lastName, grade, phone)
                    } else {
                        viewModel.addStudent(firstName, lastName, grade, phone)
                    }
                    showAddDialog = false
                    editingStudent = null
                }
            )
        }
    }
}

@Composable
fun EmptyClassroomState(onEnrollClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = BrandLightBlue.copy(alpha = 0.1f), modifier = Modifier.size(100.dp)) {
            Icon(Icons.Default.Face, contentDescription = null, tint = BrandLightBlue, modifier = Modifier.padding(24.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("The classroom is empty!", style = MaterialTheme.typography.titleLarge, color = BrandBlue, fontWeight = FontWeight.Bold)
        Text("Tap the enroll button below to add your first student.", style = MaterialTheme.typography.bodyMedium, color = ChalkGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
    }
}

@Composable
fun StudentFlashcard(student: Student, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = BrandBlue.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = student.firstName.take(1).uppercase(), color = BrandBlue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${student.firstName} ${student.lastName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BrandLightBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, BrandLightBlue.copy(alpha = 0.3f))) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.MenuBook, contentDescription = "Grade", tint = BrandLightBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = student.gradeLevel, color = BrandLightBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(14.dp), tint = ChalkGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = student.parentContact, style = MaterialTheme.typography.labelSmall, color = ChalkGray, fontWeight = FontWeight.Medium)
                }
            }

            if (student.isSynced) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Backed up", tint = BrandGreen, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartDropdownMenu(label: String, options: List<String>, selectedOption: String, onSelectionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelectionChange(option); expanded = false })
            }
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No data found") }, onClick = { expanded = false })
            }
        }
    }
}

@Composable
fun EnrollmentFormDialog(student: Student? = null, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf(student?.firstName ?: "") }
    var lastName by remember { mutableStateOf(student?.lastName ?: "") }

    val gradeOptions = listOf("Pre-K / Kinder", "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6", "Junior High", "Senior High")
    var grade by remember { mutableStateOf(student?.gradeLevel ?: gradeOptions[0]) }
    var phone by remember { mutableStateOf(student?.parentContact ?: "") }

    val isEdit = student != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Edit Student Profile" else "Enrollment Form", color = BrandBlue, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SmartDropdownMenu(label = "Grade Level", options = gradeOptions, selectedOption = grade) { grade = it }
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("Parent Contact") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (firstName.isNotBlank() && lastName.isNotBlank()) onSave(firstName, lastName, grade, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Save Changes" else "Complete Enrollment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = ChalkGray) } }
    )
}