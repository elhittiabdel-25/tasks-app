package com.ael.todo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ael.todo.data.db.AppDatabase
import com.ael.todo.data.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    fun Content() {
        val prefs = currentState<Preferences>()
        val count = prefs[PREF_COUNT] ?: 0
        val tasks = (0 until minOf(count, MAX_WIDGET_TASKS)).map { i ->
            WidgetTaskData(
                id = prefs[longPreferencesKey("t_id_$i")] ?: 0L,
                title = prefs[stringPreferencesKey("t_title_$i")] ?: "",
                time = prefs[stringPreferencesKey("t_time_$i")]
            )
        }

        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tasks Today",
                    style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                )
                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks for today",
                        style = TextStyle(color = ColorProvider(Color.White))
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(tasks) { task ->
                            WidgetTaskRow(task)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetTaskRow(task: WidgetTaskData) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = task.title,
                    style = TextStyle(color = ColorProvider(Color.White))
                )
                if (task.time != null) {
                    Text(
                        text = task.time,
                        style = TextStyle(color = ColorProvider(Color(0xFFCCCCCC)))
                    )
                }
            }
        }
    }

    data class WidgetTaskData(val id: Long, val title: String, val time: String?)

    companion object {
        val PREF_COUNT = intPreferencesKey("widget_task_count")
        const val MAX_WIDGET_TASKS = 10

        // Extension to convert dp-like values - Glance uses its own unit
        private val Int.dp get() = androidx.glance.unit.Dp(this.toFloat())

        suspend fun updateData(context: Context) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val end = cal.timeInMillis
            val db = AppDatabase.getInstance(context)
            val tasks = db.taskDao().getTodayTasksSync(start, end)
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(TasksWidget::class.java)
            ids.forEach { glanceId ->
                androidx.glance.appwidget.updateAppWidgetState(
                    context, PreferencesGlanceStateDefinition, glanceId
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[PREF_COUNT] = tasks.size
                        tasks.take(MAX_WIDGET_TASKS).forEachIndexed { i, t ->
                            this[longPreferencesKey("t_id_$i")] = t.task.id
                            this[stringPreferencesKey("t_title_$i")] = t.task.title
                            val timeStr = t.task.dueAt?.let { fmt.format(it) }
                            if (timeStr != null) this[stringPreferencesKey("t_time_$i")] = timeStr
                        }
                    }
                }
                TasksWidget().update(context, glanceId)
            }
        }
    }
}

class CompleteTaskCallback : ActionCallback {
    override suspend fun onAction(
        context: Context, glanceId: GlanceId, parameters: ActionParameters
    ) {
        val taskId = parameters[TASK_ID_KEY] ?: return
        AppDatabase.getInstance(context).taskDao().completeTask(taskId, System.currentTimeMillis())
        TasksWidget.updateData(context)
    }

    companion object {
        val TASK_ID_KEY = ActionParameters.Key<Long>("task_id")
    }
}
