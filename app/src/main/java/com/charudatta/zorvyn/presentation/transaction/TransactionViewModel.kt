package com.charudatta.zorvyn.presentation.transaction


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.usecase.DeleteTransactionUseCase
import com.charudatta.zorvyn.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val filter: String = "all" // "all", "income", "expense"
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val deleteTransaction: DeleteTransactionUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow("all")
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state

    init {
        viewModelScope.launch {
            combine(_allTransactions, _filter) { list, filter ->
                val filtered = when (filter) {
                    "income"  -> list.filter { it.type == "income" }
                    "expense" -> list.filter { it.type == "expense" }
                    else      -> list
                }
                TransactionsState(transactions = filtered, filter = filter)
            }.collect { _state.value = it }
        }

        viewModelScope.launch {
            getTransactions().collect { _allTransactions.value = it }
        }
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { deleteTransaction(transaction) }
    }
}