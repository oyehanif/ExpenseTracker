package com.hanif.expensetracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hanif.expensetracker.model.ExpenseEntity

/**
 * Room Database class
 */
@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    /**
     * Returns ExpenseDao for accessing the table
     */
    abstract fun expenseDao(): ExpenseDao
}