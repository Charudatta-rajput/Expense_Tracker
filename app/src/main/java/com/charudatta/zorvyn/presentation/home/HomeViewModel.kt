package com.charudatta.zorvyn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.data.local.GoalPreferences
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val monthlyGoal: Double = 0.0,
    val spentThisMonth: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val goalPreferences: GoalPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeTransactions()
        observeMonthlyGoal()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            getTransactions().collect { list ->
                updateTransactionStats(list)
            }
        }
    }

    private fun observeMonthlyGoal() {
        viewModelScope.launch {
            goalPreferences.monthlyGoal.collect { goal ->
                _state.update { it.copy(monthlyGoal = goal) }
            }
        }
    }

    private fun updateTransactionStats(transactions: List<Transaction>) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val income = transactions
            .filter { it.type == "income" }
            .sumOf { it.amount }

        val expense = transactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }

        val spentThisMonth = transactions
            .filter { it.type == "expense" }
            .filter { transaction ->
                val date = Date(transaction.date)
                val cal = Calendar.getInstance().apply { time = date }
                cal.get(Calendar.MONTH) == currentMonth &&
                        cal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }

        _state.update {
            it.copy(
                transactions = transactions,
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                spentThisMonth = spentThisMonth
            )
        }
    }
}