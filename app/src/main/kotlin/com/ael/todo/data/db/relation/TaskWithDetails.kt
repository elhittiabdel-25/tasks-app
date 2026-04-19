package com.ael.todo.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.Subtask
import com.ael.todo.data.db.entity.Task

data class TaskWithDetails(
    @Embedded val task: Task,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: Category?,
    @Relation(parentColumn = "id", entityColumn = "task_id")
    val subtasks: List<Subtask>
)
