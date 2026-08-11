package com.smartgrowth.app

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: StudentViewModel) {
    val payments by viewModel.payments.collectAsState()
    val students by viewModel.students.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }

    val totalRevenue = payments.sumOf { it.amount }
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance Logo", modifier = Modifier.padding(end = 8.dp))
                        Text("Finance & Billing", fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.Payments, contentDescription = "Record Payment")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Payment", fontWeight = FontWeight.Bold)
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

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Revenue", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = format.format(totalRevenue), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (payments.isEmpty()) {
                    item { Text(text = "No payments recorded yet.", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = ChalkGray) }
                } else {
                    // Using .size to avoid the Compose Compiler Type Mismatch Bug
                    items(payments.size) { index ->
                        val payment = payments[index]
                        PaymentTicket(payment = payment, onClick = { editingPayment = payment })
                    }
                }
            }
        }

        if (showAddDialog || editingPayment != null) {
            RecordPaymentDialog(
                payment = editingPayment,
                students = students,
                onDismiss = {
                    showAddDialog = false
                    editingPayment = null
                },
                onSave = { student, amount, date, method, notes ->
                    if (editingPayment != null) {
                        viewModel.updatePayment(editingPayment!!, student, amount, date, method, notes)
                    } else {
                        viewModel.addPayment(student, amount, date, method, notes)
                    }
                    showAddDialog = false
                    editingPayment = null
                }
            )
        }
    }
}

@Composable
fun PaymentTicket(payment: Payment, onClick: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = BrandGreen.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = BrandGreen, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = payment.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = "${payment.date} • ${payment.method}", style = MaterialTheme.typography.bodySmall, color = ChalkGray)
                if (payment.notes.isNotBlank()) {
                    Text(text = "Note: ${payment.notes}", style = MaterialTheme.typography.labelSmall, color = ChalkGray, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = format.format(payment.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = BrandGreen)
                Spacer(modifier = Modifier.height(4.dp))
                if (payment.isSynced) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Backed up", tint = BrandGreen, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    payment: Payment? = null, students: List<Student>,
    onDismiss: () -> Unit, onSave: (String, Double, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val studentOptions = students.map { "${it.firstName} ${it.lastName}" }
    var studentName by remember { mutableStateOf(payment?.studentName.takeIf { !it.isNullOrBlank() } ?: studentOptions.firstOrNull() ?: "") }

    var amountText by remember { mutableStateOf(payment?.amount?.toString() ?: "") }

    val methodOptions = listOf("Cash", "GCash", "Bank Transfer")
    var method by remember { mutableStateOf(payment?.method ?: methodOptions[0]) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var date by remember { mutableStateOf(payment?.date ?: today) }
    var notes by remember { mutableStateOf(payment?.notes ?: "") }

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth -> date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val isEdit = payment != null

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = BrandGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Edit Payment" else "Record Payment", color = BrandGreen, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                SmartDropdownMenu(label = "Select Student", options = studentOptions, selectedOption = studentName) { studentName = it }

                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it }, label = { Text("Amount (₱)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                SmartDropdownMenu(label = "Payment Method", options = methodOptions, selectedOption = method) { method = it }

                Box {
                    OutlinedTextField(
                        value = date, onValueChange = {}, readOnly = true, label = { Text("Date Paid") },
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (studentName.isNotBlank() && amount != null && amount > 0) {
                        onSave(studentName, amount, date, method, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen), shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Update Payment" else "Save Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = ChalkGray) } }
    )
}