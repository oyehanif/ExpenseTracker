package com.hanif.expensetracker.db

import androidx.room.*
import com.hanif.expensetracker.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("""
        SELECT COUNT(*) FROM expenses 
        WHERE date = :date 
        AND LOWER(title) = LOWER(:title) 
        AND LOWER(category) = LOWER(:category)
    """)
    fun countDuplicates(date: Long, title: String, category: String): Flow<Int>

    // New queries for expense list
    @Query("SELECT * FROM expenses WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getExpensesByDate(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :startOfDay AND date < :endOfDay ORDER BY category, date DESC")
    fun getExpensesByDateGroupedByCategory(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY category, date DESC")
    fun getAllExpensesGroupedByCategory(): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getTotalAmountByDate(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalAmount(): Double?

    @Query("SELECT COUNT(*) FROM expenses WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getCountByDate(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getTotalCount(): Int

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}
