package com.hanif.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.hanif.expensetracker.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val reportRepository: ExpenseRepository,
    @ApplicationContext private val appContext: Context
) : BaseViewModel<ExpenseReportUiState, ReportEvent>() {

    override fun createInitialState() = ExpenseReportUiState()

    init {
        observeReport()
    }

    private fun observeReport() {
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            reportRepository.reportFlow(uiState.value.reportPeriodDays)
                .catch { e ->
                    updateState { copy(isLoading = false, error = e.message ?: "Failed to load") }
                }
                .collect { report ->
                    updateState { copy(reportData = report, isLoading = false, error = null) }
                }
        }
    }


    fun onAction(action: ReportAction) {
        when (action) {
            ReportAction.LoadReport -> loadReport()
            ReportAction.ExportToPdf -> exportToPdf()
            ReportAction.ExportToCsv -> exportToCsv()
            ReportAction.ShareReport -> shareReport()
            ReportAction.DismissShareDialog -> updateState { copy(showShareDialog = false) }
            is ReportAction.ChangePeriod -> changePeriod(action.days)
            ReportAction.Refresh -> refresh()
        }
    }

    private fun loadReport() {
        updateState { copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val reportData = reportRepository.generateReport(uiState.value.reportPeriodDays)
                updateState { 
                    copy(
                        reportData = reportData,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                updateState { 
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load report"
                    ) 
                }
            }
        }
    }

    private fun exportToPdf() {
        updateState { copy(isExporting = true) }
        
        viewModelScope.launch {
            try {
                val fileName = reportRepository.exportToPdf(context = appContext)
                updateState { copy(isExporting = false) }
                sendEvent(ReportEvent.ExportCompleted(fileName, "PDF"))
            } catch (e: Exception) {
                updateState { copy(isExporting = false) }
                sendEvent(ReportEvent.ShowError("Failed to export PDF: ${e.message}"))
            }
        }
    }

    private fun exportToCsv() {
        updateState { copy(isExporting = true) }
        
        viewModelScope.launch {
            try {
                val fileName = reportRepository.exportToCsv(appContext)
                updateState { copy(isExporting = false) }
                sendEvent(ReportEvent.ExportCompleted(fileName, "CSV"))
            } catch (e: Exception) {
                updateState { copy(isExporting = false) }
                sendEvent(ReportEvent.ShowError("Failed to export CSV: ${e.message}"))
            }
        }
    }

    private fun shareReport() {
        val reportData = uiState.value.reportData ?: return
        
        val shareContent = buildString {
            appendLine("📊 Expense Report")
            appendLine("================")
            appendLine("Period: ${reportData.reportPeriod}")
            appendLine("Total Amount: ₹${String.format("%.2f", reportData.totalAmount)}")
            appendLine("Total Expenses: ${reportData.totalExpenses}")
            appendLine()
            
            appendLine("📅 Daily Summary:")
            reportData.dailyTotals.forEach { daily ->
                appendLine("${daily.date}: ₹${String.format("%.2f", daily.totalAmount)} (${daily.expenseCount} expenses)")
            }
            
            appendLine()
            appendLine("📂 Category Summary:")
            reportData.categoryTotals.forEach { category ->
                appendLine("${category.category}: ₹${String.format("%.2f", category.totalAmount)} (${String.format("%.1f", category.percentage)}%)")
            }
        }
        
        viewModelScope.launch {
            sendEvent(
                ReportEvent.ShareContent(
                    content = shareContent,
                    subject = "Expense Report - ${reportData.reportPeriod}"
                )
            )
        }
    }

    private fun changePeriod(days: Int) {
        updateState { copy(reportPeriodDays = days) }
        loadReport()
    }

    private fun refresh() {
        loadReport()
    }
}