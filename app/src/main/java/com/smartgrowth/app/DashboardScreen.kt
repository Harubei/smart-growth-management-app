package com.smartgrowth.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: StudentViewModel) {
    val students by viewModel.students.collectAsState()
    val tutors by viewModel.tutors.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    val context = LocalContext.current

    // NEW: Controls the Export Confirmation Popup
    var showExportDialog by remember { mutableStateOf(false) }

    // Date Logic for filtering sessions
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todaysSessions = sessions.filter { it.date == today }.sortedBy { it.startTime }
    val upcomingSessions = sessions.filter { it.date > today && it.status == "Scheduled" }.sortedBy { it.date }.take(3)
    val finishedSessions = sessions.filter { it.status == "Completed" }.sortedByDescending { it.date }.take(3)

    val totalRevenue = payments.sumOf { it.amount }
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

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
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Export to ZIP", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.performFullSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync to Cloud", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlue, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(NotebookBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                if (syncStatus != "Idle") {
                    Surface(color = BrandLightBlue, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = syncStatus, modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelMedium, color = Color.White,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Total Students", students.size.toString(), Icons.Default.Group, BrandLightBlue, Modifier.weight(1f))
                    StatCard("Active Staff", tutors.size.toString(), Icons.Default.Person, BrandGreen, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Classes Today", todaysSessions.size.toString(), Icons.Default.Event, BrandBlue, Modifier.weight(1f))
                    StatCard("Total Revenue", format.format(totalRevenue), Icons.Default.Payments, Color(0xFFF59E0B), Modifier.weight(1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(bottom = 8.dp))
                if (todaysSessions.isEmpty()) {
                    Text("No classes scheduled for today.", style = MaterialTheme.typography.bodyMedium, color = ChalkGray, modifier = Modifier.padding(start = 4.dp))
                }
            }
            items(todaysSessions) { session ->
                SessionTicket(session = session, onClick = { /* View only on Dashboard */ })
            }

            if (upcomingSessions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upcoming This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(upcomingSessions) { session ->
                    SessionTicket(session = session, onClick = { })
                }
            }

            if (finishedSessions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recently Finished", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(finishedSessions) { session ->
                    SessionTicket(session = session, onClick = { })
                }
            }
        }

        // NEW: Export Confirmation Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                containerColor = Color.White,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = BrandBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Database", color = BrandBlue, fontWeight = FontWeight.Bold)
                    }
                },
                text = { Text("Are you sure you want to download a complete ZIP backup of all Students, Tutors, Sessions, and Payments to this tablet?") },
                confirmButton = {
                    Button(
                        onClick = {
                            exportDataToCSV(context, students, tutors, sessions, payments)
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Export", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) { Text("Cancel", color = ChalkGray) }
                }
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = ChalkGray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        }
    }
}