package com.smartgrowth.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

fun parseAvailability(avail: String): Map<String, Pair<String, String>> {
    if (avail.isBlank()) return emptyMap()
    return avail.split(";").associate {
        val parts = it.split("|")
        if (parts.size == 3) parts[0] to Pair(parts[1], parts[2]) else "" to Pair("", "")
    }.filterKeys { it.isNotEmpty() }
}

fun serializeAvailability(map: Map<String, Pair<String, String>>): String {
    return map.entries.joinToString(";") { "${it.key}|${it.value.first}|${it.value.second}" }
}

fun pickTime(context: Context, defaultTime: String, onTimeSelected: (String) -> Unit) {
    var hour = 8
    var minute = 0
    try {
        val isPM = defaultTime.contains("PM", ignoreCase = true)
        val parts = defaultTime.replace(" AM", "").replace(" PM", "").replace(" am", "").replace(" pm", "").split(":")
        hour = parts[0].toInt()
        if (isPM && hour != 12) hour += 12
        if (!isPM && hour == 12) hour = 0
        minute = parts[1].toInt()
    } catch (_: Exception) {} // Safe underscore prevents compiler warnings

    android.app.TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            onTimeSelected(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, amPm))
        },
        hour, minute, false
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorScreen(viewModel: StudentViewModel) {
    val tutors by viewModel.tutors.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTutor by remember { mutableStateOf<Tutor?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Eco, contentDescription = "Logo", tint = BrandGreen, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Smart Growth", fontWeight = FontWeight.ExtraBold)
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
                containerColor = BrandGreen, contentColor = Color.White, shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Staff", fontWeight = FontWeight.Bold)
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
                if (tutors.isEmpty()) {
                    item { EmptyStaffState() }
                } else {
                    items(tutors) { tutor ->
                        TutorIDCard(tutor = tutor, onClick = { editingTutor = tutor })
                    }
                }
            }
        }

        if (showAddDialog || editingTutor != null) {
            TutorOnboardingDialog(
                tutor = editingTutor,
                onDismiss = {
                    showAddDialog = false
                    editingTutor = null
                },
                onSave = { firstName, middleName, lastName, phone, email, capacity, availability, subjects ->
                    val safeCapacity = capacity.toIntOrNull() ?: 1
                    if (editingTutor != null) {
                        viewModel.updateTutor(editingTutor!!, firstName, middleName, lastName, phone, email, safeCapacity, availability, subjects)
                    } else {
                        viewModel.addTutor(firstName, middleName, lastName, phone, email, safeCapacity, availability, subjects)
                    }
                    showAddDialog = false
                    editingTutor = null
                }
            )
        }
    }
}

@Composable
fun EmptyStaffState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = BrandGreen.copy(alpha = 0.1f), modifier = Modifier.size(100.dp)) {
            Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.padding(24.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("The staff room is empty!", style = MaterialTheme.typography.titleLarge, color = BrandBlue, fontWeight = FontWeight.Bold)
        Text("Tap the button below to add your first tutor.", style = MaterialTheme.typography.bodyMedium, color = ChalkGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
    }
}

@Composable
fun TutorIDCard(tutor: Tutor, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top // Changed to Top to align well with the schedule list
        ) {
            Surface(shape = CircleShape, color = BrandGreen.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = tutor.firstName.take(1).uppercase(), color = BrandGreen, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val midInitial = if(tutor.middleName.isNotBlank()) "${tutor.middleName.take(1).uppercase()}." else ""
                val formattedName = "${tutor.firstName} $midInitial ${tutor.lastName}".replace("  ", " ")

                Text(text = formattedName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BrandLightBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, BrandLightBlue.copy(alpha = 0.3f))) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = "Capacity", tint = BrandLightBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val capText = if (tutor.maxCapacity > 1) "Group (${tutor.maxCapacity})" else "1-on-1"
                            Text(text = capText, color = BrandLightBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(14.dp), tint = ChalkGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tutor.phone, style = MaterialTheme.typography.labelSmall, color = ChalkGray, fontWeight = FontWeight.Medium)
                }

                if (tutor.hireDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Hired: ${tutor.hireDate}", style = MaterialTheme.typography.labelSmall, color = ChalkGray, fontWeight = FontWeight.Bold)
                }

                if (tutor.subjectsHandled.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "SUBJECTS: ${tutor.subjectsHandled.uppercase()}", style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontWeight = FontWeight.Bold)
                }

                // Dynamic Availability Schedule Display
                val parsedSchedule = parseAvailability(tutor.availability)
                if (parsedSchedule.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    parsedSchedule.forEach { (day, times) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Time", modifier = Modifier.size(14.dp), tint = BrandGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = day, style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569), fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                            Text(text = "${times.first} - ${times.second}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No availability set.", style = MaterialTheme.typography.labelSmall, color = ChalkGray)
                }
            }

            if (tutor.isSynced) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Backed up", tint = BrandGreen, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun TutorOnboardingDialog(tutor: Tutor? = null, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(tutor?.firstName ?: "") }
    var middleName by remember { mutableStateOf(tutor?.middleName ?: "") }
    var lastName by remember { mutableStateOf(tutor?.lastName ?: "") }
    var phone by remember { mutableStateOf(tutor?.phone ?: "") }
    var email by remember { mutableStateOf(tutor?.email ?: "") }

    val capacityOptions = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    var capacity by remember { mutableStateOf(tutor?.maxCapacity?.toString() ?: capacityOptions[0]) }

    // New Subject Checklist Setup
    val subjectOptions = listOf("Reading and Writing", "Math and Science", "Homework Assistance", "Exam Preparation")
    var subjectsSet by remember { mutableStateOf(tutor?.subjectsHandled?.split(", ")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()) }

    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // We use a Map to track which day is selected, and what its specific Start/End time is!
    var scheduleMap by remember {
        mutableStateOf(parseAvailability(tutor?.availability ?: ""))
    }

    // NEW: Smart Toggle for Static vs Dynamic Hours
    val uniqueTimes = scheduleMap.values.toSet()
    var isSameHours by remember { mutableStateOf(uniqueTimes.size <= 1) }
    var sharedStartTime by remember { mutableStateOf(uniqueTimes.firstOrNull()?.first ?: "08:00 AM") }
    var sharedEndTime by remember { mutableStateOf(uniqueTimes.firstOrNull()?.second ?: "05:00 PM") }

    fun applySharedTimes(start: String, end: String) {
        sharedStartTime = start
        sharedEndTime = end
        scheduleMap = scheduleMap.mapValues { Pair(start, end) }.toMutableMap()
    }

    val isEdit = tutor != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Edit Staff Details" else "Staff Onboarding", color = BrandBlue, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp).verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = middleName, onValueChange = { middleName = it }, label = { Text("Middle Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                SmartDropdownMenu(label = "Max Capacity", options = capacityOptions, selectedOption = capacity) { capacity = it }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))
                Text("Subjects Handled", style = MaterialTheme.typography.labelMedium, color = BrandBlue, fontWeight = FontWeight.Bold)

                Column(modifier = Modifier.fillMaxWidth().background(NotebookBackground, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    subjectOptions.forEach { subject ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = subjectsSet.contains(subject),
                                onCheckedChange = { checked ->
                                    subjectsSet = if (checked) subjectsSet + subject else subjectsSet - subject
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                            )
                            Text(subject, style = MaterialTheme.typography.labelMedium, color = Color(0xFF0F172A))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))

                // Toggle between Static (Fast) and Dynamic schedules
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        isSameHours = !isSameHours
                        if (isSameHours) applySharedTimes(sharedStartTime, sharedEndTime)
                    },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Work Schedule", style = MaterialTheme.typography.labelMedium, color = ChalkGray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Same every day", style = MaterialTheme.typography.labelSmall, color = if (isSameHours) BrandBlue else ChalkGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isSameHours,
                            onCheckedChange = {
                                isSameHours = it
                                if (it) applySharedTimes(sharedStartTime, sharedEndTime)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen)
                        )
                    }
                }

                // If they want static hours, show ONE master time picker at the top!
                if (isSameHours) {
                    Surface(color = NotebookBackground, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), color = Color.White,
                                modifier = Modifier.clickable { pickTime(context, sharedStartTime) { applySharedTimes(it, sharedEndTime) } }
                            ) { Text(sharedStartTime, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = BrandBlue, fontWeight = FontWeight.Bold) }

                            Text(" to ", style = MaterialTheme.typography.labelMedium, color = ChalkGray, modifier = Modifier.padding(horizontal = 8.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), color = Color.White,
                                modifier = Modifier.clickable { pickTime(context, sharedEndTime) { applySharedTimes(sharedStartTime, it) } }
                            ) { Text(sharedEndTime, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = BrandBlue, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                // Generates a row for every day of the week
                allDays.forEach { day ->
                    val isSelected = scheduleMap.containsKey(day)
                    val times = scheduleMap[day] ?: Pair(sharedStartTime, sharedEndTime)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val newMap = scheduleMap.toMutableMap()
                                if (checked) {
                                    newMap[day] = if (isSameHours) Pair(sharedStartTime, sharedEndTime) else Pair("08:00 AM", "05:00 PM")
                                } else {
                                    newMap.remove(day)
                                }
                                scheduleMap = newMap
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                        )
                        Text(day, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF0F172A) else ChalkGray, modifier = Modifier.width(40.dp))

                        // If NOT same hours, show individual pickers next to each selected day
                        if (isSelected && !isSameHours) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Start Time Picker
                            Surface(
                                shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable {
                                    pickTime(context, times.first) { newTime ->
                                        val m = scheduleMap.toMutableMap(); m[day] = Pair(newTime, times.second); scheduleMap = m
                                    }
                                }
                            ) { Text(times.first, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue) }

                            Text(" to ", style = MaterialTheme.typography.labelSmall, color = ChalkGray, modifier = Modifier.padding(horizontal = 4.dp))

                            // End Time Picker
                            Surface(
                                shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable {
                                    pickTime(context, times.second) { newTime ->
                                        val m = scheduleMap.toMutableMap(); m[day] = Pair(times.first, newTime); scheduleMap = m
                                    }
                                }
                            ) { Text(times.second, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val serializedSchedule = serializeAvailability(scheduleMap)
                        val serializedSubjects = subjectsSet.joinToString(", ")
                        onSave(firstName, middleName, lastName, phone, email, capacity, serializedSchedule, serializedSubjects)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Save Changes" else "Hire Tutor", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = ChalkGray) } }
    )
}