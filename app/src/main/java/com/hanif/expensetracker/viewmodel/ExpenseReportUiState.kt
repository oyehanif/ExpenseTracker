package com.hanif.expensetracker.viewmodel

import java.time.LocalDate

data class ExpenseReportUiState(
    val reportData: ReportData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExporting: Boolean = false,
    val showShareDialog: Boolean = false,
    val reportPeriodDays: Int = 7
)

sealed class ReportAction {
    object LoadReport : ReportAction()
    object ExportToPdf : ReportAction()
    object ExportToCsv : ReportAction()
    object ShareReport : ReportAction()
    object DismissShareDialog : ReportAction()
    data class ChangePeriod(val days: Int) : ReportAction()
    object Refresh : ReportAction()
}

sealed class ReportEvent {
    data class ShowError(val message: String) : ReportEvent()
    data class ExportCompleted(val fileName: String, val format: String) : ReportEvent()
    data class ShareContent(val content: String, val subject: String) : ReportEvent()
}

data class ReportData(
    val dailyTotals: List<DailyTotal>,
    val categoryTotals: List<CategoryTotal>,
    val totalAmount: Double,
    val totalExpenses: Int,
    val reportPeriod: String,
    val generatedAt: Long = System.currentTimeMillis()
)

data class DailyTotal(
    val date: LocalDate,
    val totalAmount: Double,
    val expenseCount: Int
)

data class CategoryTotal(
    val category: String,
    val totalAmount: Double,
    val expenseCount: Int,
    val percentage: Double
)