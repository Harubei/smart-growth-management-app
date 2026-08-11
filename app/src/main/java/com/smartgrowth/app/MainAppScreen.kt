package com.smartgrowth.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainAppScreen(viewModel: StudentViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = BrandBlue
            ) {
                // NEW: Dashboard Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = BrandLightBlue.copy(alpha = 0.2f))
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Students") },
                    label = { Text("Students") },
                    selected = currentRoute == "students",
                    onClick = { navController.navigate("students") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = BrandLightBlue.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tutors") },
                    label = { Text("Tutors") },
                    selected = currentRoute == "tutors",
                    onClick = { navController.navigate("tutors") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = BrandLightBlue.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Sessions") },
                    label = { Text("Sessions") },
                    selected = currentRoute == "sessions",
                    onClick = { navController.navigate("sessions") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = BrandLightBlue.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance") },
                    label = { Text("Finance") },
                    selected = currentRoute == "finance",
                    onClick = { navController.navigate("finance") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandGreen, selectedTextColor = BrandGreen, indicatorColor = BrandGreen.copy(alpha = 0.2f))
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard", // App now boots directly to the Dashboard!
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(viewModel = viewModel) }
            composable("students") { StudentScreen(viewModel = viewModel) }
            composable("tutors") { TutorScreen(viewModel = viewModel) }
            composable("sessions") { SessionScreen(viewModel = viewModel) }
            composable("finance") { FinanceScreen(viewModel = viewModel) }
        }
    }
}