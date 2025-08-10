package com.hanif.expensetracker.repository

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.hanif.expensetracker.db.ExpenseDao
import com.hanif.expensetracker.model.ExpenseEntity
import com.hanif.expensetracker.viewmodel.CategoryTotal
import com.hanif.expensetracker.viewmodel.DailyTotal
import com.hanif.expensetracker.viewmodel.ReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject

class ExpenseRepository @Inject constructor(private val dao: ExpenseDao) {

    fun checkDuplicate(date: Long, title: String, category: String): Flow<Boolean> {
        return dao.countDuplicates(date, title, category).map { it > 0 }
    }

    suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    fun getExpensesByDate(date: LocalDate): Flow<List<ExpenseEntity>> {
        val (startOfDay, endOfDay) = getDateRange(date)
        return dao.getExpensesByDate(startOfDay, endOfDay)
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return dao.getAllExpenses()
    }

    fun getExpensesByDateGroupedByCategory(date: LocalDate): Flow<Map<String, List<ExpenseEntity>>> {
        val (startOfDay, endOfDay) = getDateRange(date)
        return dao.getExpensesByDateGroupedByCategory(startOfDay, endOfDay)
            .map { expenses -> expenses.groupBy { it.category } }
    }

    fun getAllExpensesGroupedByCategory(): Flow<Map<String, List<ExpenseEntity>>> {
        return dao.getAllExpensesGroupedByCategory()
            .map { expenses -> expenses.groupBy { it.category } }
    }

    suspend fun getTotalStats(date: LocalDate? = null): Pair<Int, Double> {
        return if (date != null) {
            val (startOfDay, endOfDay) = getDateRange(date)
            val count = dao.getCountByDate(startOfDay, endOfDay)
            val amount = dao.getTotalAmountByDate(startOfDay, endOfDay) ?: 0.0
            Pair(count, amount)
        } else {
            val count = dao.getTotalCount()
            val amount = dao.getTotalAmount() ?: 0.0
            Pair(count, amount)
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    private fun getDateRange(date: LocalDate): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return Pair(startOfDay, endOfDay)
    }

    suspend fun generateReport(days: Int): ReportData {
        // small delay only for demo; remove for production
        // delay(1000)

        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)

        // load all expenses in the whole period (one DB query)
        val (startEpoch, endEpochExclusive) = getDateRangeInclusive(startDate, endDate)
        val expensesInRange = dao.getExpensesByDate(startEpoch, endEpochExclusive).first()

        // compute daily totals
        val zone = ZoneId.systemDefault()
        val dailyMap = mutableMapOf<LocalDate, MutableList<ExpenseEntity>>()
        expensesInRange.forEach { e ->
            val localDate = Instant.ofEpochMilli(e.date).atZone(zone).toLocalDate()
            dailyMap.getOrPut(localDate) { mutableListOf() }.add(e)
        }

        val dailyTotals = mutableListOf<DailyTotal>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            val list = dailyMap[current].orEmpty()
            val total = list.sumOf { it.amount }
            val count = list.size
            dailyTotals.add(DailyTotal(date = current, totalAmount = total, expenseCount = count))
            current = current.plusDays(1)
        }

        // compute category totals
        val categoryGroups = expensesInRange.groupBy { it.category ?: "Unknown" }
        val totalAmount = expensesInRange.sumOf { it.amount }
        val categoryTotals = categoryGroups.map { (cat, list) ->
            val amt = list.sumOf { it.amount }
            val count = list.size
            val percentage = if (totalAmount > 0.0) (amt / totalAmount) * 100.0 else 0.0
            CategoryTotal(
                category = cat,
                totalAmount = amt,
                expenseCount = count,
                percentage = percentage
            )
        }.sortedByDescending { it.totalAmount }

        val totalExpenses = expensesInRange.size
        return ReportData(
            dailyTotals = dailyTotals,
            categoryTotals = categoryTotals,
            totalAmount = totalAmount,
            totalExpenses = totalExpenses,
            reportPeriod = "Last $days days ($startDate to $endDate)"
        )
    }

    fun reportFlow(days: Int): Flow<ReportData> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)
        val (startEpoch, endEpochExclusive) = getDateRangeInclusive(startDate, endDate)

        // dao.getExpensesByDate returns Flow<List<ExpenseEntity>>
        return dao.getExpensesByDate(startEpoch, endEpochExclusive).map { expensesInRange ->
            buildReportFromExpenses(expensesInRange, startDate, endDate, days)
        }
    }

    private fun buildReportFromExpenses(
        expensesInRange: List<ExpenseEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        days: Int
    ): ReportData {
        val zone = ZoneId.systemDefault()

        // daily totals
        val dailyMap = expensesInRange.groupBy { e ->
            Instant.ofEpochMilli(e.date).atZone(zone).toLocalDate()
        }

        val dailyTotals = mutableListOf<DailyTotal>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            val list = dailyMap[current].orEmpty()
            val total = list.sumOf { it.amount }
            val count = list.size
            dailyTotals.add(DailyTotal(date = current, totalAmount = total, expenseCount = count))
            current = current.plusDays(1)
        }

        // category
        val totalAmount = expensesInRange.sumOf { it.amount }
        val categoryTotals = expensesInRange.groupBy { it.category ?: "Unknown" }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                val count = list.size
                val percentage = if (totalAmount > 0.0) (amt / totalAmount) * 100.0 else 0.0
                CategoryTotal(cat, amt, count, percentage)
            }.sortedByDescending { it.totalAmount }

        return ReportData(
            dailyTotals = dailyTotals,
            categoryTotals = categoryTotals,
            totalAmount = totalAmount,
            totalExpenses = expensesInRange.size,
            reportPeriod = "Last $days days ($startDate to $endDate)"
        )
    }

    private fun getDateRangeInclusive(start: LocalDate, end: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val startEpoch = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endEpochExclusive = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return Pair(startEpoch, endEpochExclusive)
    }


    suspend fun exportToPdf(context: Context): String = withContext(Dispatchers.IO) {
        delay(2000) // Simulate processing

        val fileName = "expense_report_${System.currentTimeMillis()}.pdf"
        val mimeType = "application/pdf"
        val pdfContent = """
        Expense Report
        -----------------
        Date: ${System.currentTimeMillis()}
        Total Expenses: ₹12,345
        (This is mock data — replace with real expenses)
    """.trimIndent()

        saveFileToDownloads(context, fileName, mimeType, pdfContent.toByteArray())
    }

    suspend fun exportToCsv(context: Context): String = withContext(Dispatchers.IO) {
        delay(1500) // Simulate processing

        val fileName = "expense_report_${System.currentTimeMillis()}.csv"
        val mimeType = "text/csv"
        val csvContent = """
        Date,Title,Category,Amount
        2025-08-10,Tea,Food,15
        2025-08-10,Bus Ticket,Travel,20
    """.trimIndent()

        saveFileToDownloads(context, fileName, mimeType, csvContent.toByteArray())
    }

    private fun saveFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        data: ByteArray
    ): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw Exception("Failed to create file")

        resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            outputStream.write(data)
            outputStream.flush()
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri.toString() // You can return this or resolve it to a file path
    }
}
