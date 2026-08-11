package com.smartgrowth.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getDayOfWeekAbbr(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return ""
        SimpleDateFormat("EEE", Locale.US).format(date)
    } catch (e: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(viewModel: StudentViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val students by viewModel.students.collectAsState()
    val tutors by viewModel.tutors.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<Session?>(null) }

    // Calendar filter state
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    var isCalendarExpanded by remember { mutableStateOf(true) }

    // Filter sessions based on calendar selection
    val filteredSessions = if (selectedDateFilter == null) {
        sessions
    } else {
        sessions.filter { it.date == selectedDateFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = "Schedule Logo", modifier = Modifier.padding(end = 8.dp))
                        Text("Class Timetable", fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.EditCalendar, contentDescription = "Book Class")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Book Class", fontWeight = FontWeight.Bold)
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

            // Interactive Monthly Calendar View
            SessionCalendarCard(
                sessions = sessions,
                selectedDate = selectedDateFilter,
                isExpanded = isCalendarExpanded,
                onToggleExpand = { isCalendarExpanded = !isCalendarExpanded },
                onDateSelected = { date ->
                    selectedDateFilter = if (selectedDateFilter == date) null else date
                },
                onClearFilter = { selectedDateFilter = null }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedDateFilter != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtered by $selectedDateFilter (${filteredSessions.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandBlue,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { selectedDateFilter = null }) {
                                Text("Show All", color = BrandLightBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (filteredSessions.isEmpty()) {
                    item { EmptyScheduleState(onAddClick = { showAddDialog = true }, isFiltered = selectedDateFilter != null) }
                } else {
                    items(filteredSessions) { session ->
                        SessionTicket(session = session, onClick = { editingSession = session })
                    }
                }
            }
        }

        if (showAddDialog || editingSession != null) {
            BookSessionDialog(
                session = editingSession,
                students = students,
                tutors = tutors,
                onDismiss = {
                    showAddDialog = false
                    editingSession = null
                },
                onSave = { student, tutor, date, start, end, program ->
                    if (editingSession != null) {
                        viewModel.updateSession(editingSession!!, student, tutor, date, start, end, program)
                    } else {
                        viewModel.addSession(student, tutor, date, start, end, program)
                    }
                    showAddDialog = false
                    editingSession = null
                }
            )
        }
    }
}

@Composable
fun SessionCalendarCard(
    sessions: List<Session>,
    selectedDate: String?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDateSelected: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val monthName = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.YEAR, currentYear)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val sessionDates = remember(sessions) {
        sessions.map { it.date }.toSet()
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Calendar Header Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleExpand() }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Expand",
                        tint = BrandBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = monthName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedDate != null) {
                        Surface(
                            color = BrandLightBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onClearFilter() }.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Reset Filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandLightBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = ChalkGray)
                    }

                    IconButton(
                        onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = ChalkGray)
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Days of Week Row
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = ChalkGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid Calculation
                val daysInMonth = remember(currentMonth, currentYear) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, currentYear)
                    cal.set(Calendar.MONTH, currentMonth)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                    Pair(maxDays, startDayOfWeek)
                }

                val totalCells = daysInMonth.first + daysInMonth.second
                val numRows = (totalCells + 6) / 7

                Column {
                    for (row in 0 until numRows) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (col in 0..6) {
                                val dayIndex = row * 7 + col - daysInMonth.second + 1
                                if (dayIndex in 1..daysInMonth.first) {
                                    val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", currentYear, currentMonth + 1, dayIndex)
                                    val hasSession = sessionDates.contains(dateStr)
                                    val isSelected = selectedDate == dateStr

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .padding(2.dp)
                                            .background(
                                                color = when {
                                                    isSelected -> BrandBlue
                                                    hasSession -> BrandLightBlue.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = if (hasSession && !isSelected) 1.dp else 0.dp,
                                                color = if (hasSession && !isSelected) BrandLightBlue else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onDateSelected(dateStr) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayIndex.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                                fontWeight = if (hasSession || isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                            )
                                            if (hasSession) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(
                                                            color = if (isSelected) Color.White else BrandGreen,
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1.2f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleState(onAddClick: () -> Unit, isFiltered: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = BrandBlue.copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
            Icon(Icons.Default.EventNote, contentDescription = null, tint = BrandBlue, modifier = Modifier.padding(20.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFiltered) "No classes on this date!" else "The schedule is clear!",
            style = MaterialTheme.typography.titleLarge, color = BrandBlue, fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isFiltered) "Try selecting another calendar day or clear the filter." else "Tap the button below to book your first session.",
            style = MaterialTheme.typography.bodyMedium, color = ChalkGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SessionTicket(session: Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
                Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = BrandLightBlue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = session.date, style = MaterialTheme.typography.labelSmall, color = ChalkGray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(text = session.startTime, style = MaterialTheme.typography.titleSmall, color = BrandBlue, fontWeight = FontWeight.ExtraBold)
            }

            Divider(color = Color(0xFFE2E8F0), modifier = Modifier.height(50.dp).width(1.dp).padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(text = session.studentName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Person, contentDescription = "Tutor", tint = ChalkGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "with ${session.tutorName}", style = MaterialTheme.typography.bodyMedium, color = ChalkGray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BrandGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.3f))) {
                        Text(text = session.program, color = BrandGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = session.status, style = MaterialTheme.typography.labelSmall, color = if (session.status == "Completed") BrandGreen else ChalkGray, fontWeight = FontWeight.Bold)
                }
            }

            if (session.isSynced) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Backed up", tint = BrandGreen, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun BookSessionDialog(
    session: Session? = null, students: List<Student>, tutors: List<Tutor>,
    onDismiss: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val studentOptions = students.map { "${it.firstName} ${it.lastName}" }
    val tutorOptions = tutors.map { it.fullName }
    val programOptions = listOf("One-on-One", "Small Group", "Homework Assistance", "Exam Prep")

    var studentName by remember { mutableStateOf(session?.studentName.takeIf { !it.isNullOrBlank() } ?: studentOptions.firstOrNull() ?: "") }
    var tutorName by remember { mutableStateOf(session?.tutorName.takeIf { !it.isNullOrBlank() } ?: tutorOptions.firstOrNull() ?: "") }
    var program by remember { mutableStateOf(session?.program ?: programOptions[0]) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var date by remember { mutableStateOf(session?.date ?: today) }
    var startTime by remember { mutableStateOf(session?.startTime ?: "10:00 AM") }
    var endTime by remember { mutableStateOf(session?.endTime ?: "11:00 AM") }

    // Tutor Availability Validation Logic
    val selectedTutor = tutors.find { it.fullName == tutorName }
    val availabilityMap = remember(selectedTutor?.availability) {
        parseAvailability(selectedTutor?.availability ?: "")
    }
    val dayOfWeekStr = remember(date) {
        getDayOfWeekAbbr(date)
    }
    val tutorDaySchedule = availabilityMap[dayOfWeekStr]

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth -> date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val startTimePickerDialog = TimePickerDialog(
        context, { _, hourOfDay, minute -> val amPm = if (hourOfDay >= 12) "PM" else "AM"; val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12; startTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm) },
        10, 0, false
    )

    val endTimePickerDialog = TimePickerDialog(
        context, { _, hourOfDay, minute -> val amPm = if (hourOfDay >= 12) "PM" else "AM"; val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12; endTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm) },
        11, 0, false
    )

    val isEdit = session != null

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditCalendar, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Reschedule / Edit" else "Schedule Class", color = BrandBlue, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                SmartDropdownMenu(label = "Select Student", options = studentOptions, selectedOption = studentName) { studentName = it }
                SmartDropdownMenu(label = "Assign Tutor", options = tutorOptions, selectedOption = tutorName) { tutorName = it }

                // Live Tutor Availability Feedback Banner
                if (selectedTutor != null) {
                    Surface(
                        color = when {
                            tutorDaySchedule != null -> BrandGreen.copy(alpha = 0.1f)
                            availabilityMap.isNotEmpty() -> Color(0xFFFEF3C7)
                            else -> NotebookBackground
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                tutorDaySchedule != null -> BrandGreen.copy(alpha = 0.4f)
                                availabilityMap.isNotEmpty() -> Color(0xFFF59E0B)
                                else -> Color(0xFFE2E8F0)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    tutorDaySchedule != null -> Icons.Default.CheckCircle
                                    availabilityMap.isNotEmpty() -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    tutorDaySchedule != null -> BrandGreen
                                    availabilityMap.isNotEmpty() -> Color(0xFFD97706)
                                    else -> ChalkGray
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    tutorDaySchedule != null -> "${selectedTutor.fullName} is working $dayOfWeekStr: ${tutorDaySchedule.first} - ${tutorDaySchedule.second}"
                                    availabilityMap.isNotEmpty() -> "⚠️ ${selectedTutor.fullName} is NOT scheduled on $dayOfWeekStr"
                                    else -> "No specific availability set for ${selectedTutor.fullName}."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    tutorDaySchedule != null -> Color(0xFF15803D)
                                    availabilityMap.isNotEmpty() -> Color(0xFF92400E)
                                    else -> ChalkGray
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                SmartDropdownMenu(label = "Program Type", options = programOptions, selectedOption = program) { program = it }

                Box {
                    OutlinedTextField(
                        value = date, onValueChange = {}, readOnly = true, label = { Text("Date (YYYY-MM-DD)") },
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startTime, onValueChange = {}, readOnly = true, label = { Text("Start Time") },
                            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Start Time") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { startTimePickerDialog.show() })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endTime, onValueChange = {}, readOnly = true, label = { Text("End Time") },
                            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "End Time") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { endTimePickerDialog.show() })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (studentName.isNotBlank() && tutorName.isNotBlank()) onSave(studentName, tutorName, date, startTime, endTime, program) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Save Changes" else "Confirm Booking", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = ChalkGray) } }
    )
}