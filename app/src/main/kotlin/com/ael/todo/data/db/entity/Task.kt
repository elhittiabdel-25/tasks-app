package com.ael.todo.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority { NONE, LOW, MEDIUM, HIGH }
enum class TaskStatus { PENDING, COMPLETED }
enum class Recurrence { NONE, DAILY, WEEKLY }

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.SET_DEFAULT
    )],
    indices = [Index("category_id"), Index("status"), Index("due_at")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String? = null,
    @ColumnInfo(name = "due_at") val dueAt: Long? = null,
    val priority: Priority = Priority.NONE,
    @ColumnInfo(name = "category_id", defaultValue = "1") val categoryId: Long = 1L,
    val status: TaskStatus = TaskStatus.PENDING,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
