package com.ael.todo.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ael.todo.data.db.relation.TaskWithDetails
import com.ael.todo.data.repository.TaskRepository
import com.ael.todo.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val tasks = repository.getTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
