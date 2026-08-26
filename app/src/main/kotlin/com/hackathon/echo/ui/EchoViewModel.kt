package com.hackathon.echo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.echo.data.AppDatabase
import com.hackathon.echo.data.EchoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EchoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).echoDao()

    val echoes: StateFlow<List<EchoItem>> = dao.getAllEchoes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteEcho(echoItem: EchoItem) {
        viewModelScope.launch {
            dao.delete(echoItem)
        }
    }

    fun updateEchoStatus(echoItem: EchoItem, newStatus: String) {
        viewModelScope.launch {
            dao.insert(echoItem.copy(status = newStatus))
        }
    }
}


