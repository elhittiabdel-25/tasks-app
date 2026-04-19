package com.ael.todo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ael.todo.data.db.entity.Subtask

@Dao
interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subtask: Subtask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subtasks: List<Subtask>)

    @Update
    suspend fun update(subtask: Subtask)

    @Delete
    suspend fun delete(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE task_id = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("UPDATE subtasks SET is_done = :isDone WHERE id = :subtaskId")
    suspend fun setDone(subtaskId: Long, isDone: Boolean)
}
