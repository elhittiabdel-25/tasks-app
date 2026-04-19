package com.ael.todo.data.db

import androidx.room.TypeConverter
import com.ael.todo.data.db.entity.Priority
import com.ael.todo.data.db.entity.Recurrence
import com.ael.todo.data.db.entity.TaskStatus

class Converters {
    @TypeConverter fun fromPriority(v: Priority): String = v.name
    @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)

    @TypeConverter fun fromStatus(v: TaskStatus): String = v.name
    @TypeConverter fun toStatus(v: String): TaskStatus = TaskStatus.valueOf(v)

    @TypeConverter fun fromRecurrence(v: Recurrence): String = v.name
    @TypeConverter fun toRecurrence(v: String): Recurrence = Recurrence.valueOf(v)
}
