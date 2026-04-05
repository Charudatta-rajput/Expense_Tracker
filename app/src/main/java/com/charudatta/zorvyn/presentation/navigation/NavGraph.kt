package com.charudatta.zorvyn.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charudatta.zorvyn.presentation.goals.GoalsScreen
import com.charudatta.zorvyn.presentation.home.HomeScreen
import com.charudatta.zorvyn.presentation.insights.InsightsScreen
import com.charudatta.zorvyn.presentation.transaction.TransactionsScreen
import com.charudatta.zorvyn.presentation.transaction.add.AddTransactionScreen
import androidx.navigation.navDeepLink

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
                TransactionsScreen(navController)
            }
            composable(Screen.Goals.route) {
                GoalsScreen(navController)
            }
            composable(Screen.Insights.route) {
                InsightsScreen(navController)
            }

            // Normal Add Transaction (without pre-filled data)
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(navController)
            }

            // Deep link route for SMS detection (with pre-filled data)
            composable(
                route = "add_transaction_sms?amount={amount}&type={type}&note={note}",
                arguments = listOf(
                    navArgument("amount") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    },
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = "expense"
                        nullable = true
                    },
                    navArgument("note") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "zorvyn://add" }
                )
            ) { backStackEntry ->
                val amount = backStackEntry.arguments?.getString("amount") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: "expense"
                val note = backStackEntry.arguments?.getString("note") ?: ""

                AddTransactionScreen(
                    navController = navController,
                    preFilledAmount = amount,
                    preFilledType = type,
                    preFilledCategory = note
                )
            }
        }
    }
}