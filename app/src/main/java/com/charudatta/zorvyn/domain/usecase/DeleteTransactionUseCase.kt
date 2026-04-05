package com.charudatta.zorvyn.domain.usecase

import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.repository.FinanceRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}