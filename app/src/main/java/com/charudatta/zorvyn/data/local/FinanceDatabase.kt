package com.charudatta.zorvyn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.charudatta.zorvyn.data.local.dao.TransactionDao
import com.charudatta.zorvyn.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
}