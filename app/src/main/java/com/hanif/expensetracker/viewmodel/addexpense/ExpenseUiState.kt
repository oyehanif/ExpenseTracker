package com.hanif.expensetracker.viewmodel.addexpense

import android.net.Uri

data class ExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val category: String = "Staff",
    val notes: String = "",
    val receiptUri: Uri? = null,
    val isDuplicate: Boolean = false
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object ExpenseSaved : UiEvent()
}

sealed class ExpenseAction {
    data class TitleChanged(val value: String) : ExpenseAction()
    data class AmountChanged(val value: String) : ExpenseAction()
    data class CategoryChanged(val value: String) : ExpenseAction()
    data class NotesChanged(val value: String) : ExpenseAction()
    data class ReceiptPicked(val uri: Uri) : ExpenseAction()
    object Submit : ExpenseAction()
}
