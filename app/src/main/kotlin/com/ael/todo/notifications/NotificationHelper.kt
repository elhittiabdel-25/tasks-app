package com.ael.todo.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ael.todo.MainActivity
import com.ael.todo.R
import com.ael.todo.TasksApplication.Companion.NOTIFICATION_CHANNEL_ID

object NotificationHelper {

    fun build(context: Context, taskId: Long, title: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.ael.todo.OPEN_TASK"
            putExtra("task_id", taskId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Task due")
            .setContentText(title)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}
