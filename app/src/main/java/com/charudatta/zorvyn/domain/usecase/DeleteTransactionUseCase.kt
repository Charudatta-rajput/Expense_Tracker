package com.charudatta.zorvyn.domain.usecase

import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.repository.FinanceRepository

class DeleteTransactionUseCase(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}