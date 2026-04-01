package com.charudatta.zorvyn.domain.usecase

import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getTransactions()
    }
}