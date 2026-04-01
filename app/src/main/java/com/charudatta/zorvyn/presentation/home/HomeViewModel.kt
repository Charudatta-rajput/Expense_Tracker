package com.charudatta.zorvyn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val transactions: List<com.charudatta.zorvyn.domain.model.Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            getTransactions().collect { list ->

                val income = list
                    .filter { it.type == "income" }
                    .sumOf { it.amount }

                val expense = list
                    .filter { it.type == "expense" }
                    .sumOf { it.amount }

                _state.value = HomeState(
                    transactions = list,
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense
                )
            }
        }
    }
}