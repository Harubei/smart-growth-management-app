package com.smartgrowth.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)

        // FIX: We are now passing all FOUR DAOs!
        val factory = StudentViewModelFactory(
            database.studentDao(),
            database.tutorDao(),
            database.sessionDao(),
            database.paymentDao()
        )
        val viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}