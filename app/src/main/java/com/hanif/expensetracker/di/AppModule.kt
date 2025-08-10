package com.hanif.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.hanif.expensetracker.db.ExpenseDao
import com.hanif.expensetracker.db.ExpenseTrackerDatabase
import com.hanif.expensetracker.repository.ExpenseRepository
import com.hanif.expensetracker.utils.EncryptionUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides Room database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseTrackerDatabase {
        return Room.databaseBuilder(
            context, ExpenseTrackerDatabase::class.java, "expense_database"
        ).fallbackToDestructiveMigration().build()
    }

    /**
     * Provides ExpenseDao instance
     */
    @Provides
    @Singleton
    fun provideExpenseDao(database: ExpenseTrackerDatabase): ExpenseDao {
        return database.expenseDao()
    }

    /**
     * Provides EncryptionManager instance
     */
    @Provides
    @Singleton
    fun provideEncryptionManager(): EncryptionUtils {
        return EncryptionUtils()
    }

    /**
     * Provides ExpenseRepository instance
     */
    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao
    ): ExpenseRepository {
        return ExpenseRepository(expenseDao)
    }

}