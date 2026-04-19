package com.ael.todo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ael.todo.data.db.entity.Task
import com.ael.todo.data.db.entity.TaskStatus
import com.ael.todo.data.db.relation.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'PENDING' AND due_at >= :startOfDay AND due_at < :endOfDay
        ORDER BY due_at ASC
    """)
    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'PENDING' AND due_at >= :startOfDay AND due_at < :endOfDay
        ORDER BY due_at ASC
    """)
    suspend fun getTodayTasksSync(startOfDay: Long, endOfDay: Long): List<TaskWithDetails>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'PENDING'
        ORDER BY category_id ASC,
                 CASE WHEN due_at IS NULL THEN 1 ELSE 0 END ASC,
                 due_at ASC,
                 created_at ASC
    """)
    fun getAllPendingTasks(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'COMPLETED' AND completed_at >= :since
        ORDER BY completed_at DESC
    """)
    fun getCompletedTasks(since: Long): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'PENDING' AND category_id = :categoryId
        ORDER BY CASE WHEN due_at IS NULL THEN 1 ELSE 0 END ASC, due_at ASC, created_at ASC
    """)
    fun getTasksByCategory(categoryId: Long): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskWithDetails?

    @Query("UPDATE tasks SET status = 'COMPLETED', completed_at = :completedAt WHERE id = :taskId")
    suspend fun completeTask(taskId: Long, completedAt: Long)

    @Query("UPDATE tasks SET status = 'PENDING', completed_at = NULL WHERE id = :taskId")
    suspend fun uncompleteTask(taskId: Long)

    @Query("DELETE FROM tasks WHERE status = 'COMPLETED' AND completed_at < :before")
    suspend fun deleteOldCompleted(before: Long): Int
}
