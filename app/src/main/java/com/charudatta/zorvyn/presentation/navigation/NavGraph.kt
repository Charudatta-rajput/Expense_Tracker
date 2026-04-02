package com.charudatta.zorvyn.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charudatta.zorvyn.presentation.goals.GoalsScreen
import com.charudatta.zorvyn.presentation.home.HomeScreen
import com.charudatta.zorvyn.presentation.insights.InsightsScreen
import com.charudatta.zorvyn.presentation.transaction.TransactionsScreen
import com.charudatta.zorvyn.presentation.transaction.add.AddTransactionScreen

sealed class Screen(val route: String) {
    object Home           : Screen("home")
    object Transactions   : Screen("transactions")
    object Goals          : Screen("goals")
    object Insights       : Screen("insights")
    object AddTransaction : Screen("add_transaction")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen()
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            // AddTransaction is NOT a tab — it's pushed on top
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(navController)
            }
        }
    }
}