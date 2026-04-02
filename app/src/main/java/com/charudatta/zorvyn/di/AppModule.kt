package com.charudatta.zorvyn.di

import android.content.Context
import androidx.room.Room
import com.charudatta.zorvyn.data.local.FinanceDatabase
import com.charudatta.zorvyn.data.local.dao.TransactionDao
import com.charudatta.zorvyn.domain.repository.FinanceRepositoryImpl
import com.charudatta.zorvyn.domain.repository.FinanceRepository
import com.charudatta.zorvyn.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanceDatabase {
        return Room.databaseBuilder(
            context,
            FinanceDatabase::class.java,
            "finance_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: FinanceDatabase): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: TransactionDao): FinanceRepository {
        return FinanceRepositoryImpl(dao)
    }

    // 🔥 USE CASES

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(repo: FinanceRepository) =
        GetTransactionsUseCase(repo)

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(repo: FinanceRepository) =
        AddTransactionUseCase(repo)

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(repo: FinanceRepository) =
        DeleteTransactionUseCase(repo)

    @Provides
    @Singleton
    fun provideUpdateTransactionUseCase(repo: FinanceRepository) =
        UpdateTransactionUseCase(repo)
}