package com.ael.todo.ui.screen.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.Priority
import com.ael.todo.data.db.entity.Recurrence
import com.ael.todo.data.db.entity.Subtask
import com.ael.todo.data.db.entity.Task
import com.ael.todo.data.db.entity.TaskStatus
import com.ael.todo.data.repository.TaskRepository
import com.ael.todo.domain.CategoryMatcher
import com.ael.todo.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubtaskDraft(val id: Long = 0, var title: String, var isDone: Boolean = false)

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val matcher: CategoryMatcher,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    var taskId by mutableLongStateOf(-1L)
    var title by mutableStateOf("")
    var notes by mutableStateOf("")
    var dueAt by mutableStateOf<Long?>(null)
    var priority by mutableStateOf(Priority.NONE)
    var categoryId by mutableLongStateOf(1L)
    var recurrence by mutableStateOf(Recurrence.NONE)
    val subtasks = mutableStateListOf<SubtaskDraft>()

    val categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(id: Long) = viewModelScope.launch {
        taskId = id
        val details = repository.getTaskById(id) ?: return@launch
        val t = details.task
        title = t.title
        notes = t.notes ?: ""
        dueAt = t.dueAt
        priority = t.priority
        categoryId = t.categoryId
        recurrence = t.recurrence
        subtasks.clear()
        subtasks.addAll(details.subtasks.sortedBy { it.position }.map {
            SubtaskDraft(it.id, it.title, it.isDone)
        })
    }

    fun onTitleChange(value: String) {
        title = value
        viewModelScope.launch {
            val cats = categories.value
            val keywords = repository.getAllKeywords()
            val matched = matcher.match(value, keywords, cats)
            if (matched != null) categoryId = matched.id
        }
    }

    fun addSubtask() { subtasks.add(SubtaskDraft(title = "")) }
    fun removeSubtask(index: Int) { if (index in subtasks.indices) subtasks.removeAt(index) }
    fun setSubtaskTitle(index: Int, value: String) { if (index in subtasks.indices) subtasks[index] = subtasks[index].copy(title = value) }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val task = Task(
            id = if (taskId > 0) taskId else 0,
            title = title.trim(),
            notes = notes.trim().ifBlank { null },
            dueAt = dueAt,
            priority = priority,
            categoryId = categoryId,
            status = TaskStatus.PENDING,
            recurrence = recurrence
        )
        val subs = subtasks.mapIndexed { i, d ->
            Subtask(id = d.id, taskId = taskId.coerceAtLeast(0), title = d.title, isDone = d.isDone, position = i)
        }
        val savedId = if (taskId > 0) {
            alarmScheduler.cancel(taskId)
            repository.updateTask(task, subs)
            taskId
        } else {
            repository.insertTask(task, subs)
        }
        dueAt?.let { alarmScheduler.schedule(savedId, it, task.title) }
        onDone()
    }
}
