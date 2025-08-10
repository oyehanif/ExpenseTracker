package com.hanif.expensetracker.viewmodel.expenselisting

import com.hanif.expensetracker.model.ExpenseEntity
import java.time.LocalDate

data class ExpenseListUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val groupedExpenses: Map<String, List<ExpenseEntity>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val groupBy: GroupBy = GroupBy.NONE,
    val filterType: FilterType = FilterType.TODAY,
    val totalCount: Int = 0,
    val totalAmount: Double = 0.0,
    val showDatePicker: Boolean = false
)

enum class GroupBy { NONE, CATEGORY, TIME }
enum class FilterType { TODAY, CUSTOM_DATE, ALL_TIME }

sealed class ExpenseListAction {
    object LoadTodayExpenses : ExpenseListAction()
    data class LoadExpensesByDate(val date: LocalDate) : ExpenseListAction()
    object LoadAllExpenses : ExpenseListAction()
    data class ToggleGroupBy(val groupBy: GroupBy) : ExpenseListAction()
    data class FilterChanged(val filterType: FilterType) : ExpenseListAction()
    data class DateSelected(val date: LocalDate) : ExpenseListAction()
    object ShowDatePicker : ExpenseListAction()
    object HideDatePicker : ExpenseListAction()
    object Refresh : ExpenseListAction()
}

sealed class ExpenseListEvent {
    data class ShowError(val message: String) : ExpenseListEvent()
    object NavigateToAddExpense : ExpenseListEvent()
}