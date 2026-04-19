package com.ael.todo.domain

import com.ael.todo.data.db.entity.Recurrence
import com.ael.todo.data.db.entity.Task
import com.ael.todo.data.db.entity.TaskStatus
import java.util.Calendar

object RecurrenceHelper {

    fun nextInstance(task: Task, completedAt: Long): Task {
        val baseDueAt = task.dueAt ?: completedAt
        val nextDueAt = when (task.recurrence) {
            Recurrence.DAILY -> addDays(baseDueAt, 1)
            Recurrence.WEEKLY -> addDays(baseDueAt, 7)
            Recurrence.NONE -> baseDueAt
        }
        return task.copy(
            id = 0,
            status = TaskStatus.PENDING,
            completedAt = null,
            dueAt = nextDueAt,
            createdAt = completedAt
        )
    }

    fun addDays(epochMillis: Long, days: Int): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        cal.add(Calendar.DAY_OF_MONTH, days)
        return cal.timeInMillis
    }
}
