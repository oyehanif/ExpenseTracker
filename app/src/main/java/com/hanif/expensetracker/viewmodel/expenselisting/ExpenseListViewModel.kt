package com.hanif.expensetracker.viewmodel.expenselisting

import androidx.lifecycle.viewModelScope
import com.hanif.expensetracker.repository.ExpenseRepository
import com.hanif.expensetracker.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : BaseViewModel<ExpenseListUiState, ExpenseListEvent>() {

    override fun createInitialState() = ExpenseListUiState()

    init {
        onAction(ExpenseListAction.LoadTodayExpenses)
    }

    fun onAction(action: ExpenseListAction) {
        when (action) {
            is ExpenseListAction.LoadTodayExpenses -> loadTodayExpenses()
            is ExpenseListAction.LoadExpensesByDate -> loadExpensesByDate(action.date)
            is ExpenseListAction.LoadAllExpenses -> loadAllExpenses()
            is ExpenseListAction.ToggleGroupBy -> toggleGroupBy(action.groupBy)
            is ExpenseListAction.FilterChanged -> changeFilter(action.filterType)
            is ExpenseListAction.DateSelected -> selectDate(action.date)
            is ExpenseListAction.ShowDatePicker -> updateState { copy(showDatePicker = true) }
            is ExpenseListAction.HideDatePicker -> updateState { copy(showDatePicker = false) }
            is ExpenseListAction.Refresh -> refresh()
        }
    }

    private fun loadTodayExpenses() {
        updateState { 
            copy(
                isLoading = true, 
                error = null, 
                filterType = FilterType.TODAY,
                selectedDate = LocalDate.now()
            ) 
        }
        
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                if (currentState.groupBy == GroupBy.CATEGORY) {
                    repository.getExpensesByDateGroupedByCategory(LocalDate.now())
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats(LocalDate.now())
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (groupedExpenses, count, amount) ->
                            updateState {
                                copy(
                                    groupedExpenses = groupedExpenses,
                                    expenses = emptyList(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                } else {
                    repository.getExpensesByDate(LocalDate.now())
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats(LocalDate.now())
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (expenses, count, amount) ->
                            updateState {
                                copy(
                                    expenses = expenses,
                                    groupedExpenses = emptyMap(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadExpensesByDate(date: LocalDate) {
        updateState { 
            copy(
                isLoading = true, 
                error = null, 
                selectedDate = date,
                filterType = FilterType.CUSTOM_DATE
            ) 
        }
        
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                if (currentState.groupBy == GroupBy.CATEGORY) {
                    repository.getExpensesByDateGroupedByCategory(date)
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats(date)
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (groupedExpenses, count, amount) ->
                            updateState {
                                copy(
                                    groupedExpenses = groupedExpenses,
                                    expenses = emptyList(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                } else {
                    repository.getExpensesByDate(date)
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats(date)
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (expenses, count, amount) ->
                            updateState {
                                copy(
                                    expenses = expenses,
                                    groupedExpenses = emptyMap(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadAllExpenses() {
        updateState { 
            copy(
                isLoading = true, 
                error = null, 
                filterType = FilterType.ALL_TIME
            ) 
        }
        
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                if (currentState.groupBy == GroupBy.CATEGORY) {
                    repository.getAllExpensesGroupedByCategory()
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats()
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (groupedExpenses, count, amount) ->
                            updateState {
                                copy(
                                    groupedExpenses = groupedExpenses,
                                    expenses = emptyList(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                } else {
                    repository.getAllExpenses()
                        .combine(flowOf(Unit)) { expenses, _ ->
                            val stats = repository.getTotalStats()
                            Triple(expenses, stats.first, stats.second)
                        }
                        .collect { (expenses, count, amount) ->
                            updateState {
                                copy(
                                    expenses = expenses,
                                    groupedExpenses = emptyMap(),
                                    totalCount = count,
                                    totalAmount = amount,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun toggleGroupBy(groupBy: GroupBy) {
        updateState { copy(groupBy = groupBy) }
        refresh()
    }

    private fun changeFilter(filterType: FilterType) {
        when (filterType) {
            FilterType.TODAY -> loadTodayExpenses()
            FilterType.CUSTOM_DATE -> updateState { copy(showDatePicker = true) }
            FilterType.ALL_TIME -> loadAllExpenses()
        }
    }

    private fun selectDate(date: LocalDate) {
        updateState { copy(showDatePicker = false) }
        loadExpensesByDate(date)
    }

    private fun refresh() {
        when (uiState.value.filterType) {
            FilterType.TODAY -> loadTodayExpenses()
            FilterType.CUSTOM_DATE -> loadExpensesByDate(uiState.value.selectedDate)
            FilterType.ALL_TIME -> loadAllExpenses()
        }
    }
}