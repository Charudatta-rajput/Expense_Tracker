package com.charudatta.zorvyn.data.repository

import com.charudatta.zorvyn.data.local.dao.TransactionDao
import com.charudatta.zorvyn.data.mapper.toDomain
import com.charudatta.zorvyn.data.mapper.toEntity
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepositoryImpl(
    private val dao: TransactionDao
) : FinanceRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }
}