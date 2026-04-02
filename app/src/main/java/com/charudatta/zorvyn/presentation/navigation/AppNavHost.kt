package com.charudatta.zorvyn.presentation.navigation

// presentation/navigation/AppNavHost.kt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charudatta.zorvyn.presentation.goals.GoalsScreen
import com.charudatta.zorvyn.presentation.home.HomeScreen
import com.charudatta.zorvyn.presentation.insights.InsightsScreen
import com.charudatta.zorvyn.presentation.transaction.TransactionsScreen

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route)         { HomeScreen(navController) }
        composable(Screen.Transactions.route) { TransactionsScreen() }
        composable(Screen.Goals.route)        { GoalsScreen() }
        composable(Screen.Insights.route)     { InsightsScreen() }
    }
}