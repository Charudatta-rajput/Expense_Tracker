package com.charudatta.zorvyn.domain.repository

import com.charudatta.zorvyn.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {

    fun getTransactions(): Flow<List<Transaction>>

    suspend fun insertTransaction(transaction: Transaction)

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)
}