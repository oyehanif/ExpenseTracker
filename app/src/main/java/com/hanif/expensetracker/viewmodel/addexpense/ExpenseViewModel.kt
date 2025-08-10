package com.hanif.expensetracker.viewmodel.addexpense

import androidx.lifecycle.viewModelScope
import com.hanif.expensetracker.model.ExpenseEntity
import com.hanif.expensetracker.repository.ExpenseRepository
import com.hanif.expensetracker.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel  @Inject constructor(
    private val repo: ExpenseRepository
) : BaseViewModel<ExpenseUiState, UiEvent>() {

    private var currentDate: Long = System.currentTimeMillis()

    override fun createInitialState() = ExpenseUiState()

    init {
        observeDuplicates()
    }

    fun setDate(date: Long) {
        currentDate = date
    }

    fun onAction(action: ExpenseAction) {
        when (action) {
            is ExpenseAction.TitleChanged -> updateState { copy(title = action.value) }
            is ExpenseAction.AmountChanged -> if (action.value.all { it.isDigit() || it == '.' }) {
                updateState { copy(amount = action.value) }
            }
            is ExpenseAction.CategoryChanged -> updateState { copy(category = action.value) }
            is ExpenseAction.NotesChanged -> if (action.value.length <= 100) {
                updateState { copy(notes = action.value) }
            }
            is ExpenseAction.ReceiptPicked -> updateState { copy(receiptUri = action.uri) }
            ExpenseAction.Submit -> submitExpense()
        }
    }

    private fun observeDuplicates() {
        combine(
            uiState.map { it.title }.distinctUntilChanged(),
            uiState.map { it.category }.distinctUntilChanged()
        ) { title, category ->
            Pair(title, category)
        }.flatMapLatest { (title, category) ->
            if (title.isNotBlank()) repo.checkDuplicate(currentDate, title, category)
            else flowOf(false)
        }.onEach { duplicate ->
            updateState { copy(isDuplicate = duplicate) }
        }.launchIn(viewModelScope)
    }

    private fun submitExpense() {
        val state = uiState.value
        val error = validate(state)
        if (error != null) {
            sendEvent(UiEvent.ShowToast(error))
            return
        }

        viewModelScope.launch {
            repo.insertExpense(
                ExpenseEntity(
                    title = state.title.trim(),
                    amount = state.amount.toDouble(),
                    category = state.category,
                    notes = state.notes.takeIf { it.isNotBlank() },
                    receiptImageUri = state.receiptUri?.toString(),
                    date = currentDate
                )
            )
            sendEvent(UiEvent.ShowToast("Expense added successfully"))
            sendEvent(UiEvent.ExpenseSaved)
        }
    }

    private fun validate(state: ExpenseUiState): String? {
        if (state.title.isBlank() || state.amount.isBlank()) return "Title and Amount are required"
        if (state.isDuplicate) return "Duplicate entry found"
        return null
    }
}
