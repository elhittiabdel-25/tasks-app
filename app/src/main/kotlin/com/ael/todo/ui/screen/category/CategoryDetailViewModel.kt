package com.ael.todo.ui.screen.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.relation.TaskWithDetails
import com.ael.todo.data.repository.TaskRepository
import com.ael.todo.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L

    val tasks = repository.getTasksByCategory(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val category: StateFlow<Category?> = repository.getCategories()
        .map { cats -> cats.firstOrNull { it.id == categoryId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun complete(taskWithDetails: TaskWithDetails) = viewModelScope.launch {
        repository.completeTask(taskWithDetails.task.id)
        alarmScheduler.cancel(taskWithDetails.task.id)
    }

    fun delete(taskWithDetails: TaskWithDetails) = viewModelScope.launch {
        repository.deleteTask(taskWithDetails.task)
        alarmScheduler.cancel(taskWithDetails.task.id)
    }

    fun undoDelete(taskWithDetails: TaskWithDetails) = viewModelScope.launch {
        repository.insertTask(taskWithDetails.task, taskWithDetails.subtasks)
        taskWithDetails.task.dueAt?.let {
            alarmScheduler.schedule(taskWithDetails.task.id, it, taskWithDetails.task.title)
        }
    }
}
