package com.example.mytablebookingpradita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytablebookingpradita.data.remote.ApiState
import com.example.mytablebookingpradita.data.remote.TableRepository
import com.example.mytablebookingpradita.data.remote.TableResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TableApiViewModel(private val repository: TableRepository) : ViewModel() {
    private val _tableState = MutableStateFlow<ApiState<List<TableResponse>>>(ApiState.Empty)
    val tableState: StateFlow<ApiState<List<TableResponse>>> = _tableState

    fun getTables(roomName: String) {
        _tableState.value = ApiState.Loading
        viewModelScope.launch {
            // Simulasi pemanggilan API ke Backend
            _tableState.value = repository.fetchTables(roomName)
        }
    }
}

class TableApiViewModelFactory(private val repository: TableRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TableApiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TableApiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}