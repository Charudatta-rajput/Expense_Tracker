package com.charudatta.zorvyn.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.charudatta.zorvyn.presentation.home.HomeScreen
import com.charudatta.zorvyn.presentation.transaction.add.AddTransactionScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
}

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController)
        }
    }
}