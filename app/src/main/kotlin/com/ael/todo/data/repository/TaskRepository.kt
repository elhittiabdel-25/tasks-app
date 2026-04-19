package com.ael.todo.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.ael.todo.data.db.dao.CategoryDao
import com.ael.todo.data.db.dao.CategoryKeywordDao
import com.ael.todo.data.db.dao.SubtaskDao
import com.ael.todo.data.db.dao.TaskDao
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.CategoryKeyword
import com.ael.todo.data.db.entity.Recurrence
import com.ael.todo.data.db.entity.Subtask
import com.ael.todo.data.db.entity.Task
import com.ael.todo.data.db.entity.TaskStatus
import com.ael.todo.data.db.relation.CategoryWithKeywords
import com.ael.todo.data.db.relation.TaskWithDetails
import com.ael.todo.domain.RecurrenceHelper
import com.ael.todo.widget.TasksWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val categoryDao: CategoryDao,
    private val keywordDao: CategoryKeywordDao,
    @ApplicationContext private val context: Context
) {

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return start to cal.timeInMillis
    }

    fun getTodayTasks(): Flow<List<TaskWithDetails>> {
        val (s, e) = todayRange()
        return taskDao.getTodayTasks(s, e)
    }

    fun getAllPendingTasks(): Flow<List<TaskWithDetails>> = taskDao.getAllPendingTasks()

    fun getCompletedTasks(): Flow<List<TaskWithDetails>> {
        val since = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
        return taskDao.getCompletedTasks(since)
    }

    fun getTasksByCategory(categoryId: Long): Flow<List<TaskWithDetails>> =
        taskDao.getTasksByCategory(categoryId)

    suspend fun getTaskById(id: Long): TaskWithDetails? = taskDao.getTaskById(id)

    fun getCategories(): Flow<List<Category>> = categoryDao.getAll()

    fun getAllCategoriesWithKeywords(): Flow<List<CategoryWithKeywords>> =
        categoryDao.getAllWithKeywords()

    fun getCategoryWithKeywords(id: Long): Flow<CategoryWithKeywords> =
        categoryDao.getCategoryWithKeywords(id)

    suspend fun getAllKeywords(): List<CategoryKeyword> = keywordDao.getAllSync()

    suspend fun insertTask(task: Task, subtasks: List<Subtask> = emptyList()): Long {
        val id = taskDao.insert(task)
        if (subtasks.isNotEmpty()) {
            subtaskDao.insertAll(subtasks.map { it.copy(taskId = id) })
        }
        notifyWidget()
        return id
    }

    suspend fun updateTask(task: Task, subtasks: List<Subtask>? = null) {
        taskDao.update(task)
        if (subtasks != null) {
            subtaskDao.deleteForTask(task.id)
            if (subtasks.isNotEmpty()) {
                subtaskDao.insertAll(subtasks.map { it.copy(taskId = task.id) })
            }
        }
        notifyWidget()
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
        notifyWidget()
    }

    suspend fun completeTask(taskId: Long) {
        val now = System.currentTimeMillis()
        taskDao.completeTask(taskId, now)
        val details = taskDao.getTaskById(taskId)
        if (details != null && details.task.recurrence != Recurrence.NONE) {
            val next = RecurrenceHelper.nextInstance(details.task, now)
            taskDao.insert(next)
        }
        notifyWidget()
    }

    suspend fun uncompleteTask(taskId: Long) {
        taskDao.uncompleteTask(taskId)
        notifyWidget()
    }

    suspend fun deleteOldCompleted(before: Long): Int = taskDao.deleteOldCompleted(before)

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun replaceKeywords(categoryId: Long, keywords: List<String>) {
        keywordDao.deleteForCategory(categoryId)
        keywordDao.insertAll(keywords.map { CategoryKeyword(categoryId = categoryId, keyword = it.lowercase()) })
    }

    private fun notifyWidget() {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TasksWidgetReceiver::class.java))
        if (ids.isNotEmpty()) {
            val intent = android.content.Intent(context, TasksWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
