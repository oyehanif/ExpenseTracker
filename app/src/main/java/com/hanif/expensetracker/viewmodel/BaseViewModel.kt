package com.hanif.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel<UI_STATE, UI_EVENT> : ViewModel() {

    protected abstract fun createInitialState(): UI_STATE

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<UI_STATE> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UI_EVENT>()
    val events: SharedFlow<UI_EVENT> = _events.asSharedFlow()

    protected fun updateState(reducer: UI_STATE.() -> UI_STATE) {
        _uiState.value = _uiState.value.reducer()
    }

    protected fun sendEvent(event: UI_EVENT) {
        viewModelScope.launch { _events.emit(event) }
    }
}
