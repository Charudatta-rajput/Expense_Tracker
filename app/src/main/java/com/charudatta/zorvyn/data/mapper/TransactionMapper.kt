package com.charudatta.zorvyn.data.mapper

import com.charudatta.zorvyn.data.local.entity.TransactionEntity
import com.charudatta.zorvyn.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(id, amount, type, category, date, note)
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(id, amount, type, category, date, note)
}