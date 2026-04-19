package com.ael.todo.ui.screen.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ael.todo.data.db.relation.TaskWithDetails
import com.ael.todo.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks = repository.getCompletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun uncomplete(taskWithDetails: TaskWithDetails) = viewModelScope.launch {
        repository.uncompleteTask(taskWithDetails.task.id)
    }

    fun delete(taskWithDetails: TaskWithDetails) = viewModelScope.launch {
        repository.deleteTask(taskWithDetails.task)
    }
}
