package com.charudatta.zorvyn.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.charudatta.zorvyn.presentation.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            ) {
                Text("+")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Balance: ₹${state.balance}", style = MaterialTheme.typography.headlineMedium)

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Income: ₹${state.totalIncome}")
                    Text("Expense: ₹${state.totalExpense}")
                }
            }

            Text("Recent Transactions")

            state.transactions.take(5).forEach {
                Text("${it.category} - ₹${it.amount}")
            }
        }
    }
}