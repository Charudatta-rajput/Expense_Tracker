package com.charudatta.zorvyn.presentation.transaction.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase
) : ViewModel() {

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String
    ) {
        viewModelScope.launch {
            addTransaction(
                Transaction(
                    id = 0,
                    amount = amount,
                    type = type,
                    category = category,
                    date = System.currentTimeMillis(),
                    note = note
                )
            )
        }
    }
}